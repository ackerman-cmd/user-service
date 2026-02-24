-- liquibase formatted sql

-- changeset ackerman:003-create-users


CREATE TABLE user_service.users
(
    id                UUID                      NOT NULL PRIMARY KEY,
    email             VARCHAR(255)              NOT NULL UNIQUE,
    username          VARCHAR(64)               NOT NULL UNIQUE,
    password_hash     VARCHAR(255)              NOT NULL,
    first_name        VARCHAR(64),
    last_name         VARCHAR(64),
    status            VARCHAR(64),
    email_verified    BOOLEAN                   NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP                 NOT NULL,
    updated_at        TIMESTAMP                 NOT NULL
);

CREATE INDEX idx_users_email ON user_service.users (email);
CREATE INDEX idx_users_username ON user_service.users (username);
CREATE INDEX idx_users_status ON user_service.users (status);