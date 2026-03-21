-- liquibase formatted sql

-- changeset ackerman:012-seed-demo-users
-- comment: Демо-аккаунты для стенда/защиты. Пароль у всех: Demo123!
--          Логины: demo_admin_1, demo_admin_2, demo_operator_1, demo_operator_2
--          На фронте можно открыть /login?username=demo_admin_1 и ввести пароль вручную (не передавайте пароль в URL).

INSERT INTO user_service.users (id, email, username, password_hash, first_name, last_name, status, email_verified,
                                created_at, updated_at)
VALUES ('a0000001-0000-4000-8000-000000000001'::uuid,
        'demo.admin1@demo.local',
        'demo_admin_1',
        '$2a$10$unIftUmApSqfX.4aV..SjOL7pJ6r.SjAjotjrsolAZWHLga4wAtgy',
        'Demo',
        'Admin One',
        'ACTIVE',
        true,
        now(),
        now()),
       ('a0000001-0000-4000-8000-000000000002'::uuid,
        'demo.admin2@demo.local',
        'demo_admin_2',
        '$2a$10$unIftUmApSqfX.4aV..SjOL7pJ6r.SjAjotjrsolAZWHLga4wAtgy',
        'Demo',
        'Admin Two',
        'ACTIVE',
        true,
        now(),
        now()),
       ('a0000001-0000-4000-8000-000000000003'::uuid,
        'demo.operator1@demo.local',
        'demo_operator_1',
        '$2a$10$unIftUmApSqfX.4aV..SjOL7pJ6r.SjAjotjrsolAZWHLga4wAtgy',
        'Demo',
        'Operator One',
        'ACTIVE',
        true,
        now(),
        now()),
       ('a0000001-0000-4000-8000-000000000004'::uuid,
        'demo.operator2@demo.local',
        'demo_operator_2',
        '$2a$10$unIftUmApSqfX.4aV..SjOL7pJ6r.SjAjotjrsolAZWHLga4wAtgy',
        'Demo',
        'Operator Two',
        'ACTIVE',
        true,
        now(),
        now());

INSERT INTO user_service.user_roles (user_id, role_id, assigned_at)
SELECT u.id, r.id, now()
FROM user_service.users u
         CROSS JOIN user_service.roles r
WHERE u.username = 'demo_admin_1'
  AND r.name IN ('ROLE_USER', 'ROLE_ADMIN');

INSERT INTO user_service.user_roles (user_id, role_id, assigned_at)
SELECT u.id, r.id, now()
FROM user_service.users u
         CROSS JOIN user_service.roles r
WHERE u.username = 'demo_admin_2'
  AND r.name IN ('ROLE_USER', 'ROLE_ADMIN');

INSERT INTO user_service.user_roles (user_id, role_id, assigned_at)
SELECT u.id, r.id, now()
FROM user_service.users u
         CROSS JOIN user_service.roles r
WHERE u.username = 'demo_operator_1'
  AND r.name IN ('ROLE_USER', 'ROLE_OPERATOR');

INSERT INTO user_service.user_roles (user_id, role_id, assigned_at)
SELECT u.id, r.id, now()
FROM user_service.users u
         CROSS JOIN user_service.roles r
WHERE u.username = 'demo_operator_2'
  AND r.name IN ('ROLE_USER', 'ROLE_OPERATOR');
