-- liquibase formatted sql

-- changeset ackerman:011-add-role-operator-and-permissions

-- Новые права: работа с обращениями и мониторинг операторов
INSERT INTO user_service.permissions (id, name, description, created_at)
VALUES (gen_random_uuid(), 'APPEAL_READ', 'Просмотр обращений', now()),
       (gen_random_uuid(), 'APPEAL_WRITE', 'Работа с обращениями: ответ, закрытие, переназначение', now()),
       (gen_random_uuid(), 'OPERATOR_MONITOR', 'Просмотр и мониторинг загруженности операторов', now());

-- Роль оператора
INSERT INTO user_service.roles (id, name, description, created_at)
VALUES (gen_random_uuid(), 'ROLE_OPERATOR', 'Оператор: работа с обращениями клиентов', now());

-- Права для ROLE_OPERATOR
INSERT INTO user_service.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM user_service.roles r,
     user_service.permissions p
WHERE r.name = 'ROLE_OPERATOR'
  AND p.name IN ('APPEAL_READ', 'APPEAL_WRITE');

-- Право OPERATOR_MONITOR для ROLE_ADMIN
INSERT INTO user_service.role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM user_service.roles r,
     user_service.permissions p
WHERE r.name = 'ROLE_ADMIN'
  AND p.name = 'OPERATOR_MONITOR';
