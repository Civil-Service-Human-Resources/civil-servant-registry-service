
CREATE TABLE IF NOT EXISTS `department` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `code` varchar(20) NOT NULL UNIQUE ,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `organisation` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `department_id` smallint DEFAULT NULL,
  `code` varchar(20) NOT NULL UNIQUE,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_organisation_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `department_organisations` (
  `department_id` smallint NOT NULL,
  `organisations_id` smallint NOT NULL,
  PRIMARY KEY (`department_id`,`organisations_id`),
  CONSTRAINT `FK_department_organisations_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_department_organisations_organisation` FOREIGN KEY (`organisations_id`) REFERENCES `organisation` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `grade` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `organisation_id` smallint DEFAULT NULL,
  `code` varchar(20) NOT NULL UNIQUE,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_grade_organisation` FOREIGN KEY (`organisation_id`) REFERENCES `organisation` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `profession` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `job_role` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `parent_id` smallint DEFAULT NULL,
  `profession_id` smallint DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_job_role_job_role` FOREIGN KEY (`parent_id`) REFERENCES `job_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_job_role_profession` FOREIGN KEY (`profession_id`) REFERENCES `profession` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `identity` (
  `id` mediumint NOT NULL AUTO_INCREMENT,
  `uid` varchar(36) NOT NULL UNIQUE,
  PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `civil_servant` (
  `id` mediumint NOT NULL AUTO_INCREMENT,
  `identity_id` mediumint NOT NULL,
  `organisation_id` smallint DEFAULT NULL,
  `grade_id` smallint DEFAULT NULL,
  `profession_id` smallint DEFAULT NULL,
  `job_role_id` smallint DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `line_manager_id` mediumint,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK_civil_servant_grade` FOREIGN KEY (`grade_id`) REFERENCES `grade` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `FK_civil_servant_identity` FOREIGN KEY
  (`job_role_id`) REFERENCES `job_role` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `FK_civil_servant_organisation` FOREIGN KEY (`organisation_id`) REFERENCES `organisation` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `FK_civil_servant_profession` FOREIGN KEY (`profession_id`) REFERENCES `profession` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  CONSTRAINT `FK_civil_servant_line_manager` FOREIGN KEY (`line_manager_id`) REFERENCES `civil_servant` (`id`) ON DELETE SET NULL ON UPDATE SET NULL
);

CREATE TABLE IF NOT EXISTS `civil_servant_other_areas_of_work` (
  `civil_servant_id` mediumint NOT NULL,
  `other_areas_of_work_id` smallint NOT NULL,
  PRIMARY KEY (`civil_servant_id`,`other_areas_of_work_id`),
  CONSTRAINT `FK_civil_servant_other_areas_of_work_civil_servant` FOREIGN KEY (`civil_servant_id`) REFERENCES `civil_servant` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `FK_civil_servant_other_areas_of_work_profession` FOREIGN KEY (`other_areas_of_work_id`) REFERENCES `profession` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);