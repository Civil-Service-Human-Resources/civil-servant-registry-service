ALTER TABLE `agency_token` ADD COLUMN `uid` varchar(36) UNIQUE;

ALTER TABLE `agency_token` DROP COLUMN `capacity_used`;

UPDATE `agency_token` SET `uid` = '6c7a36b8-b722-4315-a506-17f59f868022' where id = 1;
UPDATE `agency_token` SET `uid` = '788affc9-bf68-475d-bf5d-590f50fe0fce' where id = 2;
UPDATE `agency_token` SET `uid` = '4403b18b-f46b-4190-aaa9-e1ab6435e1d0' where id = 3;
