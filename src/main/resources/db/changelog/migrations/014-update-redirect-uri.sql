-- liquibase formatted sql
-- changeset ackerman:014-update-redirect-uri

-- Заменяем старый домен cloud.arm-support-mai.ru на arm-support-mai.ru в redirect_uris SPA-клиента.
UPDATE user_service.oauth2_registered_client
SET redirect_uris = replace(redirect_uris, 'cloud.arm-support-mai.ru', 'arm-support-mai.ru')
WHERE client_id = 'spa-client'
  AND redirect_uris LIKE '%cloud.arm-support-mai.ru%';
