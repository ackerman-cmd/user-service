-- liquibase formatted sql

-- changeset ackerman:010-create-outbox-dead-letters

CREATE TABLE user_service.outbox_dead_letters
(
    id                UUID         NOT NULL PRIMARY KEY,
    original_event_id UUID         NOT NULL,
    aggregate_type    VARCHAR(128) NOT NULL,
    aggregate_id      VARCHAR(128) NOT NULL,
    event_type        VARCHAR(128) NOT NULL,
    topic             VARCHAR(255) NOT NULL,
    payload           TEXT         NOT NULL,
    error             TEXT         NOT NULL,
    retry_count       INT          NOT NULL,
    created_at        TIMESTAMP    NOT NULL,
    failed_at         TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_dead_letters_failed_at
    ON user_service.outbox_dead_letters (failed_at);
