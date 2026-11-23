
CREATE TABLE `department_payment_methods` (
  `department_id` smallint NOT NULL,
  `payment_method` varchar(20) NOT NULL,
  PRIMARY KEY (`department_id`, `payment_method`),
  CONSTRAINT `FK_department_payment_method` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO department_payment_methods SELECT id, 'PURCHASE_ORDER' FROM department WHERE code = 'co';
INSERT INTO department_payment_methods SELECT id, 'PURCHASE_ORDER' FROM department WHERE code = 'dh';
INSERT INTO department_payment_methods SELECT id, 'PURCHASE_ORDER' FROM department WHERE code = 'hmrc';
INSERT INTO department_payment_methods SELECT id, 'FINANCIAL_APPROVER' FROM department WHERE code = 'hmrc';
