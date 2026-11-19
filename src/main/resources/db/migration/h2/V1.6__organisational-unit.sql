DROP TABLE IF EXISTS `organisational_unit`;

CREATE TABLE `organisational_unit` (
  `id` smallint NOT NULL AUTO_INCREMENT,
  `parent_id` smallint DEFAULT NULL,
  `code` varchar(20) NOT NULL,
  `abbreviation` varchar(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `payment_methods` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `unique_name` UNIQUE (`name`),
  CONSTRAINT `unique_code` UNIQUE (`code`),
  CONSTRAINT `FK_organisational_unit_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `organisational_unit` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO `organisational_unit` (`id`, `code`, `name`, `payment_methods`)
  SELECT `id`, `code`, `name`, GROUP_CONCAT(`payment_method`) AS `payment_methods`
  FROM `department`
    JOIN `department_payment_methods` `dpm`
      ON `dpm`.`department_id` = `department`.`id`
  GROUP BY id;

ALTER TABLE `grade` DROP CONSTRAINT `FK_grade_organisation`;
ALTER TABLE `grade` RENAME COLUMN `organisation_id` TO `organisational_unit_id`;
ALTER TABLE `grade` ADD CONSTRAINT `FK_grade_organisational_unit_id` FOREIGN KEY (`organisational_unit_id`) REFERENCES `organisational_unit` (`id`);

ALTER TABLE `civil_servant` DROP CONSTRAINT `FK_civil_servant_organisation`;
ALTER TABLE `civil_servant` RENAME COLUMN `organisation_id` TO `organisational_unit_id`;
ALTER TABLE `civil_servant` ADD CONSTRAINT `FK_civil_servant_organisational_unit_id` FOREIGN KEY (`organisational_unit_id`) REFERENCES `organisational_unit` (`id`);

DROP TABLE `department_organisations`;
DROP TABLE `department_payment_methods`;
DROP TABLE `organisation`;
DROP TABLE `department`;
