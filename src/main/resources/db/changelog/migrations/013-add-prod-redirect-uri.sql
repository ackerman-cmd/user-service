-- liquibase formatted sql
-- changeset ackerman:013-add-prod-redirect-uri

-- Добавляем продакшн redirect_uri для SPA-клиента.
-- Spring Authorization Server хранит несколько URI через запятую.
UPDATE user_service.oauth2_registered_client
SET redirect_uris = 'http://localhost:3000/callback,https://cloud.arm-support-mai.ru/callback'
WHERE client_id = 'spa-client'
  AND redirect_uris NOT LIKE '%cloud.arm-support-mai.ru%';
