CREATE TABLE IF NOT EXISTS `agency_domain`
(
    `id`    SMALLINT NOT NULL AUTO_INCREMENT,
    `domain` VARCHAR(150) NOT NULL,
    PRIMARY KEY (`id`)
);


  CREATE TABLE IF NOT EXISTS `agency_token`
(
    `id`    SMALLINT NOT NULL AUTO_INCREMENT,
    `token` VARCHAR (10) NOT NULL UNIQUE,
    `capacity` smallint NOT NULL,
    `capacity_used` smallint NOT NULL,
    PRIMARY KEY (`id`)
);


  CREATE TABLE `agency_token_agency_domains`
(
    `agency_token_id` SMALLINT NOT NULL,
    `agency_domains_id`  SMALLINT NOT NULL,
    PRIMARY KEY (`agency_token_id`, `agency_domains_id`),
    CONSTRAINT `FK_token_domain_token` FOREIGN KEY (`agency_token_id`) REFERENCES `agency_token` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `FK_token_domain_domains` FOREIGN KEY (`agency_domains_id`) REFERENCES `agency_domain` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

SET REFERENTIAL_INTEGRITY FALSE;

ALTER TABLE `organisational_unit`
ADD COLUMN agency_token_id SMALLINT;

ALTER TABLE `organisational_unit`
ADD CONSTRAINT `FK_organisational_unit_agency_token` FOREIGN KEY (`agency_token_id`) REFERENCES `agency_token` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

SET REFERENTIAL_INTEGRITY TRUE;

INSERT INTO agency_token (token, capacity, capacity_used) VALUES ('token123', 100, 10);
INSERT INTO agency_token (token, capacity, capacity_used) VALUES ('token456', 200, 24);
INSERT INTO agency_token (token, capacity, capacity_used) VALUES ('token4gla', 300, 24);
INSERT INTO agency_domain (domain) VALUES ('mydomain.com');
INSERT INTO agency_domain (domain) VALUES ('example.com');
INSERT INTO agency_domain (domain) VALUES ('nhsglasgow.gov.uk');

INSERT INTO agency_token_agency_domains VALUES (1, 1);
INSERT INTO agency_token_agency_domains VALUES (1, 2);
INSERT INTO agency_token_agency_domains VALUES (2, 3);

UPDATE `organisational_unit` SET agency_token_id = 1 WHERE id = 1;
UPDATE `organisational_unit` SET agency_token_id = 2 WHERE id = 2;
UPDATE `organisational_unit` SET agency_token_id = 3 WHERE id = 8;
