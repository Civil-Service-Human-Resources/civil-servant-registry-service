INSERT INTO domains (`id`, `domain`) VALUES
(1, 'cabinetoffice.gov.uk'),
(2, 'some-domain.com'),
(3, 'another-domain.co.uk');

INSERT INTO organisational_unit_domains (organisational_unit_id, domain_id) VALUES
(1, 1),
(1, 3),
(4, 2),
(2, 1),
(4, 1),
(2, 3);
