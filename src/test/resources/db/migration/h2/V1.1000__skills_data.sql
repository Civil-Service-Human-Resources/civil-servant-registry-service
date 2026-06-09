INSERT INTO `identity` (id, uid)
VALUES
    (1002, 'skills-1'),
    (1003, 'skills-2'),
    (1004, 'skills-3'),
    (1005, 'skills-4'),
    (1006, 'skills-5'),
    (1007, 'skills-6');

INSERT INTO civil_servant (id, identity_id, organisational_unit_id, grade_id, profession_id, full_name)
VALUES
    (1100, 1002, 2, 1, 1, 'Skills 1'),
    (1101, 1003, 2, 1, 1, 'Skills 2'),
    (1102, 1004, 2, 1, 1, 'Skills 3'),
    (1103, 1005, 2, 1, 1, 'Skills 4'),
    (1104, 1006, 2, 1, 1, 'Skills 5'),
    (1105, 1007, 2, 1, 1, 'Skills 6');

INSERT INTO civil_servant_skills_metadata (civil_servant_id, sync_timestamp)
VALUES
    (1100, NULL),
    (1101, NULL),
    (1102, '2022-01-01T10:00:00.00'),
    (1103, '2022-02-01T10:00:00.00'),
    (1104, '2022-03-01T10:00:00.00'),
    (1105, '2022-04-01T10:00:00.00');
