CREATE TABLE civil_servant_other_organisational_units (
      civil_servant_id INT NOT NULL,
      other_organisational_units_id SMALLINT NOT NULL,
      PRIMARY KEY (civil_servant_id, other_organisational_units_id),
      CONSTRAINT FK_civil_servant_other_organisations_civil_servant
          FOREIGN KEY (civil_servant_id)
              REFERENCES civil_servant (id)
              ON DELETE CASCADE
              ON UPDATE CASCADE,
      CONSTRAINT FK_civil_servant_other_organisations_organisation
          FOREIGN KEY (other_organisational_units_id)
              REFERENCES organisational_unit (id)
              ON DELETE CASCADE
              ON UPDATE CASCADE
);

CREATE INDEX FK_civil_servant_organisation
ON civil_servant_other_organisational_units (other_organisational_units_id);
