CREATE TABLE `civil_servant_skills_metadata` (
    `id` mediumint unsigned NOT NULL PRIMARY KEY AUTO_INCREMENT,
    `civil_servant_id` mediumint unsigned NOT NULL,
    `created_timestamp` timestamp NOT NULL DEFAULT NOW(),
    `sync_timestamp` timestamp NULL,
    CONSTRAINT `FK_civil_servant_skills_metadata_civil_servant` FOREIGN KEY (`civil_servant_id`) REFERENCES `civil_servant` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
