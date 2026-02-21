CREATE SCHEMA IF NOT EXISTS user_service;

GRANT ALL PRIVILEGES ON SCHEMA user_service TO admin;

ALTER USER admin SET search_path TO user_service;