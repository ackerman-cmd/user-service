-- liquibase formatted sql

-- changeset ackerman:002-create-roles-permissions

CREATE TABLE user_service.permissions
(
    id          UUID        NOT NULL  PRIMARY KEY,
    name        VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL
);

CREATE TABLE user_service.roles
(
    id          UUID        NOT NULL  PRIMARY KEY,
    name        VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL
);

CREATE TABLE user_service.role_permissions
(
    role_id       UUID NOT NULL REFERENCES user_service.roles (id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES user_service.permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO user_service.permissions (id, name, description, created_at)
VALUES (gen_random_uuid(),'USER_READ', 'Чтение данных пользователей', now()),
       (gen_random_uuid(),'USER_WRITE', 'Создание и редактирование пользователей', now()),
       (gen_random_uuid(),'USER_DELETE', 'Удаление пользователей', now()),
       (gen_random_uuid(),'ADMIN_ACCESS', 'Полный доступ к административным функциям', now());

INSERT INTO user_service.roles (id, name, description, created_at)
VALUES (gen_random_uuid(),'ROLE_USER', 'Стандартный пользователь', now()),
       (gen_random_uuid(),'ROLE_ADMIN', 'Администратор системы', now());

INSERT INTO user_service.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM user_service.roles r,
     user_service.permissions p
WHERE r.name = 'ROLE_USER'
  AND p.name IN ('USER_READ');

INSERT INTO user_service.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM user_service.roles r,
     user_service.permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name IN ('USER_READ', 'USER_WRITE', 'USER_DELETE', 'ADMIN_ACCESS');