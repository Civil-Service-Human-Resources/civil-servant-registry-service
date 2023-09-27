ALTER TABLE `organisational_unit`
    ADD
        (
        `created_timestamp`  TIMESTAMP NOT NULL DEFAULT NOW(),
        `updated_timestamp`  TIMESTAMP NOT NULL DEFAULT NOW()
        );

CREATE TABLE `domains`
(
    `id` smallint unsigned NOT NULL AUTO_INCREMENT,
    `domain` VARCHAR(255) NOT NULL,
    `created_timestamp`  TIMESTAMP    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (`id`),
    UNIQUE (`domain`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `organisational_unit_domains`
(
    organisational_unit_id SMALLINT(5) unsigned NOT NULL,
    domain_id SMALLINT unsigned NOT NULL,
    created_timestamp TIMESTAMP DEFAULT NOW() NOT NULL,
    PRIMARY KEY (organisational_unit_id, domain_id),
    CONSTRAINT organisational_unit_id_FK FOREIGN KEY (organisational_unit_id) REFERENCES organisational_unit(id),
    CONSTRAINT domain_id_FK FOREIGN KEY (domain_id) REFERENCES domains(id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
