CREATE TABLE `civil_servant_skills_metadata` (
    `id` mediumint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `civil_servant_id` mediumint NOT NULL,
    `created_timestamp` timestamp NOT NULL DEFAULT NOW(),
    `sync_timestamp` timestamp NULL,
    CONSTRAINT `FK_civil_servant_skills_metadata_civil_servant` FOREIGN KEY (`civil_servant_id`) REFERENCES `civil_servant` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

ALTER TABLE `organisational_unit`
    ALTER COLUMN id RESTART WITH 34;

ALTER TABLE `domains`
    ALTER COLUMN `id` RESTART WITH 4;