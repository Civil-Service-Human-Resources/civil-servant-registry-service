SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE `question` MODIFY `value` VARCHAR(500);

ALTER TABLE `question` MODIFY `theme` VARCHAR(200);

ALTER TABLE `submitted_answer` MODIFY `question` VARCHAR(15000);

SET FOREIGN_KEY_CHECKS = 1;