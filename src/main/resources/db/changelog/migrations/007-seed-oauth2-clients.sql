-- liquibase formatted sql
-- changeset ackerman:007-seed-oauth2-clients

-- SPA CLIENT (Next.js on :3000) — Authorization Code + PKCE + Refresh Token
INSERT INTO user_service.oauth2_registered_client (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_secret_expires_at,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    post_logout_redirect_uris,
    scopes,
    client_settings,
    token_settings
)
SELECT
    'spa-client-id-1',
    'spa-client',
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    'SPA Client (Next.js)',
    'none',
    'authorization_code,refresh_token',
    'http://localhost:3000/callback',
    NULL,
    'openid,profile,api',
    '{"@class":"java.util.Collections$UnmodifiableMap",
      "settings.client.require-proof-key":true,
      "settings.client.require-authorization-consent":false
     }',
    '{"@class":"java.util.Collections$UnmodifiableMap",
      "settings.token.reuse-refresh-tokens":true,
      "settings.token.access-token-time-to-live":["java.time.Duration",1800.000000000],
      "settings.token.refresh-token-time-to-live":["java.time.Duration",2592000.000000000],
      "settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],
      "settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"]
     }'
WHERE NOT EXISTS (
    SELECT 1 FROM user_service.oauth2_registered_client WHERE client_id = 'spa-client'
);

-- SERVICE CLIENT — Client Credentials
INSERT INTO user_service.oauth2_registered_client (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_secret_expires_at,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    post_logout_redirect_uris,
    scopes,
    client_settings,
    token_settings
)
SELECT
    'service-client-id-1',
    'service-client',
    CURRENT_TIMESTAMP,
    '$2a$10$4cgzfs9VRaF3hz7skT10aeLxogzlNQyE3D4QV71nU28reKKwWIw0y',
    NULL,
    'Service Client',
    'client_secret_basic',
    'client_credentials',
    NULL,
    NULL,
    'internal.read,internal.write',
    '{"@class":"java.util.Collections$UnmodifiableMap",
      "settings.client.require-authorization-consent":false
     }',
    '{"@class":"java.util.Collections$UnmodifiableMap",
      "settings.token.access-token-time-to-live":["java.time.Duration",1800.000000000]
     }'
WHERE NOT EXISTS (
    SELECT 1 FROM user_service.oauth2_registered_client WHERE client_id = 'service-client'
);