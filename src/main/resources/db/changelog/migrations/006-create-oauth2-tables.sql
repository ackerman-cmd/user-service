-- liquibase formatted sql

-- changeset ackerman:006-create-oauth2-tables-postgresql



CREATE TABLE IF NOT EXISTS user_service.oauth2_registered_client
(
    id                        VARCHAR(100)  NOT NULL PRIMARY KEY,
    client_id                 VARCHAR(100)  NOT NULL,
    client_id_issued_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_secret             VARCHAR(200),
    client_secret_expires_at  TIMESTAMP,
    client_name               VARCHAR(200)  NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris             VARCHAR(1000),
    post_logout_redirect_uris VARCHAR(1000),
    scopes                    VARCHAR(1000) NOT NULL,
    client_settings           VARCHAR(2000) NOT NULL,
    token_settings            VARCHAR(2000) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_oauth2_registered_client_client_id
    ON user_service.oauth2_registered_client (client_id);

CREATE TABLE IF NOT EXISTS user_service.oauth2_authorization
(
    id                          VARCHAR(100) NOT NULL PRIMARY KEY,
    registered_client_id        VARCHAR(100) NOT NULL,
    principal_name              VARCHAR(200) NOT NULL,
    authorization_grant_type    VARCHAR(100) NOT NULL,
    authorized_scopes           VARCHAR(1000),
    attributes                  TEXT,
    state                       VARCHAR(500),

    authorization_code_value        TEXT,
    authorization_code_issued_at    TIMESTAMP,
    authorization_code_expires_at   TIMESTAMP,
    authorization_code_metadata     TEXT,

    access_token_value              TEXT,
    access_token_issued_at          TIMESTAMP,
    access_token_expires_at         TIMESTAMP,
    access_token_metadata           TEXT,
    access_token_type               VARCHAR(100),
    access_token_scopes             VARCHAR(1000),

    oidc_id_token_value             TEXT,
    oidc_id_token_issued_at         TIMESTAMP,
    oidc_id_token_expires_at        TIMESTAMP,
    oidc_id_token_metadata          TEXT,

    refresh_token_value             TEXT,
    refresh_token_issued_at         TIMESTAMP,
    refresh_token_expires_at        TIMESTAMP,
    refresh_token_metadata          TEXT,

    user_code_value                 TEXT,
    user_code_issued_at             TIMESTAMP,
    user_code_expires_at            TIMESTAMP,
    user_code_metadata              TEXT,

    device_code_value               TEXT,
    device_code_issued_at           TIMESTAMP,
    device_code_expires_at          TIMESTAMP,
    device_code_metadata            TEXT
);

CREATE INDEX IF NOT EXISTS ix_oauth2_authorization_registered_client_id
    ON user_service.oauth2_authorization (registered_client_id);

CREATE INDEX IF NOT EXISTS ix_oauth2_authorization_principal_name
    ON user_service.oauth2_authorization (principal_name);

CREATE INDEX IF NOT EXISTS ix_oauth2_authorization_state
    ON user_service.oauth2_authorization (state);

CREATE TABLE IF NOT EXISTS user_service.oauth2_authorization_consent
(
    registered_client_id VARCHAR(100)  NOT NULL,
    principal_name       VARCHAR(200)  NOT NULL,
    authorities          VARCHAR(1000) NOT NULL,
    PRIMARY KEY (registered_client_id, principal_name)
);

CREATE INDEX IF NOT EXISTS ix_oauth2_authorization_consent_principal_name
    ON user_service.oauth2_authorization_consent (principal_name);