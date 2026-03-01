-- liquibase formatted sql

-- changeset ackerman:008-create-user-verification-tokens

CREATE TABLE user_service.user_verification_tokens
(
    id         UUID         NOT NULL PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES user_service.users (id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    used_at    TIMESTAMP    NULL
);

CREATE INDEX idx_user_verification_tokens_user_id
    ON user_service.user_verification_tokens (user_id);

