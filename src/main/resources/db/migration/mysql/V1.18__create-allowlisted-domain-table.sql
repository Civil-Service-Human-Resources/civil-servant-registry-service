CREATE TABLE IF NOT EXISTS allowlisted_domain(
    `id`        SMALLINT(5)     UNSIGNED    NOT NULL AUTO_INCREMENT,
    `domain`    VARCHAR(255)                NOT NULL,
    PRIMARY KEY (`id`)
    )
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8;