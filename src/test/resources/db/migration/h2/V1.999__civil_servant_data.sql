INSERT INTO `identity` (id, uid) VALUES(1000, 'manager');
INSERT INTO civil_servant (id, identity_id, organisational_unit_id, grade_id, profession_id, full_name) VALUES(1000, 1000, 2, 1, 1, 'Manager');

INSERT INTO `identity` (id, uid) VALUES(999, 'learner');
INSERT INTO civil_servant (identity_id, organisational_unit_id, grade_id, profession_id, full_name, line_manager_id) VALUES(999, 2, 1, 1, 'Learner', 1000);
INSERT INTO profession (id, parent_id, name) VALUES(100, 1, 'Analysis-child');
INSERT INTO profession (id, parent_id, name) VALUES(101, 2, 'Commercial-child');
INSERT INTO profession (parent_id, name) VALUES(100, 'Analysis-grandchild');
