-- liquibase formatted sql

-- changeset ackerman:004-create-user-roles

CREATE TABLE user_service.user_roles
(
    user_id    UUID NOT NULL REFERENCES user_service.users (id) ON DELETE CASCADE,
    role_id    UUID NOT NULL REFERENCES user_service.roles (id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);