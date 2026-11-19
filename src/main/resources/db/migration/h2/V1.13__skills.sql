SET REFERENTIAL_INTEGRITY FALSE;


ALTER TABLE `question_choices`
DROP CONSTRAINT `FK_QUESTION_CHOICES_CHOICE`;

ALTER TABLE `question_answers`
DROP CONSTRAINT `FK_QUESTION_ANSWERS_ANSWER`;

DROP TABLE IF EXISTS choice;

DROP TABLE IF EXISTS question_answers;

DROP TABLE IF EXISTS question_choices;

DROP TABLE IF EXISTS quiz_questions;


CREATE TABLE IF NOT EXISTS `answer`
(
    `id`    SMALLINT NOT NULL AUTO_INCREMENT,
    `question_id`    SMALLINT NOT NULL,
    `answers` VARCHAR(2500),
    `correct_answer` VARCHAR(500),
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `submitted_answer`
(
    `id`    SMALLINT NOT NULL AUTO_INCREMENT,
    `quiz_result_id`    SMALLINT NOT NULL,
    `question` VARCHAR (1500) NOT NULL,
    `submitted_answers` VARCHAR(20),
    `skipped` VARCHAR(10),
    `correct` VARCHAR(10),
    PRIMARY KEY (`id`)
);


CREATE TABLE IF NOT EXISTS `quiz_result`
(
    `id`    SMALLINT NOT NULL AUTO_INCREMENT,
    `quiz_id`    SMALLINT NOT NULL,
    `staff_id`    VARCHAR(36),
    `quiz_name` VARCHAR(150),
    `profession_id`    SMALLINT NOT NULL,
    `organisation_id`    SMALLINT NOT NULL,
    `correct_count` SMALLINT NOT NULL,
    `question_count` SMALLINT NOT NULL,
    `score_obtained` DECIMAL(5,2) NOT NULL,
    `result`  VARCHAR(15),
    `type`  VARCHAR(10),
    `completed_on` TIMESTAMP NOT NULL,
    PRIMARY KEY (`id`)
);

ALTER TABLE
  `question`
ADD
  (
    `answer` SMALLINT,
    `img_url` VARCHAR(500),
    `alternative_text` VARCHAR(500),
    `suggestions` VARCHAR(500),
    `status`  VARCHAR(15),
    `quiz_id` SMALLINT NOT NULL,
    `times_attempted` INT,
    `correct_count` INT,
    `incorrect_count` INT,
    `skipped_count` INT
  );

ALTER TABLE
  `quiz`
ADD
  (
    `name` VARCHAR(500),
    `status` VARCHAR(15),
    `result` VARCHAR(10),
    `description` VARCHAR(1500),
    `number_of_questions` SMALLINT,
    `created_on` TIMESTAMP NOT NULL DEFAULT NOW(),
    `updated_on` TIMESTAMP NOT NULL DEFAULT NOW()
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


SET REFERENTIAL_INTEGRITY TRUE;
