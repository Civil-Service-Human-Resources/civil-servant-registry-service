SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS choice;

DROP TABLE IF EXISTS question_answers;

DROP TABLE IF EXISTS question_choices;

DROP TABLE IF EXISTS quiz_questions;


CREATE TABLE IF NOT EXISTS `answer`
(
    `id`    SMALLINT(5) UNSIGNED NOT NULL AUTO_INCREMENT,
    `question_id`    SMALLINT(5) UNSIGNED NOT NULL,
    `answers` VARCHAR(2500),
    `correct_answer` VARCHAR(500),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE IF NOT EXISTS `submitted_answer`
(
    `id`    SMALLINT(5) UNSIGNED NOT NULL AUTO_INCREMENT,
    `quiz_result_id`    SMALLINT(5) UNSIGNED NOT NULL,
    `question` VARCHAR (1500) NOT NULL,
    `submitted_answers` VARCHAR(20),
    `skipped` VARCHAR(10),
    `correct` VARCHAR(10),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


CREATE TABLE IF NOT EXISTS `quiz_result`
(
    `id`    SMALLINT(5) UNSIGNED NOT NULL AUTO_INCREMENT,
    `quiz_id`    SMALLINT(5) UNSIGNED NOT NULL,
    `staff_id`    VARCHAR(36),
    `quiz_name` VARCHAR(150),
    `profession_id`    SMALLINT(5) UNSIGNED NOT NULL,
    `organisation_id`    SMALLINT(5) UNSIGNED NOT NULL,
    `correct_count` SMALLINT(5) UNSIGNED NOT NULL,
    `question_count` SMALLINT(5) UNSIGNED NOT NULL,
    `score_obtained` DECIMAL(5,2) UNSIGNED NOT NULL,
    `result`  VARCHAR(15),
    `type`  VARCHAR(10),
    `completed_on` TIMESTAMP NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

ALTER TABLE
  `question`
ADD
  (
    `answer` SMALLINT(5) UNSIGNED,
    `img_url` VARCHAR(500),
    `alternative_text` VARCHAR(500),
    `suggestions` VARCHAR(500),
    `status`  VARCHAR(15),
    `quiz_id` SMALLINT(5) UNSIGNED NOT NULL,
    `times_attempted` INT(5)  UNSIGNED,
    `correct_count` INT(5)  UNSIGNED,
    `incorrect_count` INT(5)  UNSIGNED,
    `skipped_count` INT(5)  UNSIGNED
  );

ALTER TABLE
  `quiz`
ADD
  (
    `name` VARCHAR(500),
    `status` VARCHAR(15),
    `result` VARCHAR(10),
    `description` VARCHAR(1500),
    `number_of_questions` SMALLINT(3) UNSIGNED,
    `created_on` TIMESTAMP NOT NULL,
    `updated_on` TIMESTAMP NOT NULL
  );


ALTER TABLE `answer`
ADD CONSTRAINT `FK_question` FOREIGN KEY (`question_id`)
REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE
  `question`
ADD
  (
    CONSTRAINT `FK_question_answer` FOREIGN KEY (`answer`) REFERENCES `answer` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FK_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
  );

SET FOREIGN_KEY_CHECKS = 1;
