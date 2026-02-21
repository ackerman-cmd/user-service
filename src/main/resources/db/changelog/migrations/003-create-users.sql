-- liquibase formatted sql

-- changeset user-service:003-create-users

CREATE TYPE user_service.user_status AS ENUM ('ACTIVE', 'INACTIVE', 'BLOCKED', 'PENDING_VERIFICATION');

CREATE TABLE user_service.users
(
    id                UUID                      NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email             VARCHAR(255)              NOT NULL UNIQUE,
    username          VARCHAR(64)               NOT NULL UNIQUE,
    password_hash     VARCHAR(255)              NOT NULL,
    first_name        VARCHAR(64),
    last_name         VARCHAR(64),
    status            user_service.user_status  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified    BOOLEAN                   NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255),
    created_at        TIMESTAMP                 NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP                 NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON user_service.users (email);
CREATE INDEX idx_users_username ON user_service.users (username);
CREATE INDEX idx_users_status ON user_service.users (status);