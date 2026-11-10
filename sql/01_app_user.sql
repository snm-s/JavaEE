-- =====================================================================
-- 01_app_user.sql (Module 1)
-- Table des utilisateurs de la plateforme
-- =====================================================================

CREATE TABLE IF NOT EXISTS app_user (
                                        id            BIGSERIAL PRIMARY KEY,
                                        first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'AGENT', 'ADMIN')),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email);
CREATE INDEX IF NOT EXISTS idx_app_user_role  ON app_user(role);