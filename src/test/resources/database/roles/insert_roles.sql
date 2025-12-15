INSERT INTO roles(id, role_name)
VALUES(1, 'ROLE_CUSTOMER')
ON DUPLICATE KEY UPDATE role_name = role_name;

INSERT INTO roles(id, role_name)
VALUES(2, 'ROLE_MANAGER')
ON DUPLICATE KEY UPDATE role_name = role_name;

INSERT INTO roles(id, role_name)
VALUES(3, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE role_name = role_name;
