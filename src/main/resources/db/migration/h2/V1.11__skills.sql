CREATE TABLE IF NOT EXISTS `choice`
(
    `id`    SMALLINT NOT NULL AUTO_INCREMENT,
    `value` TEXT                 NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `question`
(
    `id`                 SMALLINT NOT NULL AUTO_INCREMENT,
    `type`               VARCHAR(8),
    `learning_name`      VARCHAR(500),
    `learning_reference` VARCHAR(500),
    `value`              TEXT                 NOT NULL,
    PRIMARY KEY (`id`)
);


CREATE TABLE IF NOT EXISTS `question_answers`
(
    `question_id` SMALLINT NOT NULL,
    `answers_id`  SMALLINT NOT NULL,
    PRIMARY KEY (`question_id`, `answers_id`),
    CONSTRAINT `FK_question_answers_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FK_question_answers_answer` FOREIGN KEY (`answers_id`) REFERENCES `choice` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `question_choices`
(
    `question_id` SMALLINT NOT NULL,
    `choices_id`  SMALLINT NOT NULL,
    PRIMARY KEY (`question_id`, `choices_id`),
    CONSTRAINT `FK_question_choices_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FK_question_choices_choice` FOREIGN KEY (`choices_id`) REFERENCES `choice` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `quiz`
(
    `id`            SMALLINT NOT NULL AUTO_INCREMENT,
    `profession_id` SMALLINT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_quiz_profession` FOREIGN KEY (`profession_id`) REFERENCES `profession` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `quiz_questions`
(
    `quiz_id`      SMALLINT NOT NULL,
    `questions_id` SMALLINT NOT NULL,
    PRIMARY KEY (`quiz_id`, `questions_id`),
    CONSTRAINT `FK_quiz_questions_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FK_quiz_questions_question` FOREIGN KEY (`questions_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS `purchase_order`
(
    `id`   SMALLINT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(20),
    PRIMARY KEY (`id`),
    CONSTRAINT `unique_po_code` UNIQUE (`code`)
);

INSERT INTO purchase_order(code)
values ('123'),
       ('abc');

UPDATE `organisational_unit`
SET `payment_methods` = 'PURCHASE_ORDER';
ALTER TABLE `organisational_unit`
    ALTER `payment_methods` SET DEFAULT 'PURCHASE_ORDER';

ALTER TABLE `question`
ADD COLUMN `theme` VARCHAR (500);

ALTER TABLE `question`
ADD COLUMN `why` VARCHAR (500);


