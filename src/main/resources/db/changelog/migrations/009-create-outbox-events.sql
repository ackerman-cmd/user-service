-- liquibase formatted sql

-- changeset ackerman:009-create-outbox-events

CREATE TABLE user_service.outbox_events
(
    id             UUID         NOT NULL PRIMARY KEY,
    aggregate_type VARCHAR(128) NOT NULL,
    aggregate_id   VARCHAR(128) NOT NULL,
    event_type     VARCHAR(128) NOT NULL,
    topic          VARCHAR(255) NOT NULL,
    payload        TEXT         NOT NULL,
    retry_count    INT          NOT NULL DEFAULT 0,
    last_error     TEXT         NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT now(),
    sent_at        TIMESTAMP    NULL
);

CREATE INDEX idx_outbox_events_unsent
    ON user_service.outbox_events (created_at)
    WHERE sent_at IS NULL;
