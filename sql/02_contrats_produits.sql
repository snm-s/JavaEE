-- =====================================================================
-- 02_contrats_produits.sql (Module 2)
-- Types d'assurance, garanties et contrats souscrits
-- =====================================================================

CREATE TABLE IF NOT EXISTS insurance_type (
                                              id          BIGSERIAL PRIMARY KEY,
                                              code        VARCHAR(20)   NOT NULL UNIQUE,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    description TEXT,
    base_price  NUMERIC(10,2) NOT NULL CHECK (base_price > 0),
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS coverage (
                                        id            BIGSERIAL PRIMARY KEY,
                                        code          VARCHAR(30)  NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    coverage_type VARCHAR(50),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS insurance_type_coverage (
                                                       id                 BIGSERIAL PRIMARY KEY,
                                                       insurance_type_id  BIGINT        NOT NULL REFERENCES insurance_type(id) ON DELETE CASCADE,
    coverage_id        BIGINT        NOT NULL REFERENCES coverage(id) ON DELETE RESTRICT,
    is_default         BOOLEAN       NOT NULL DEFAULT FALSE,
    additional_premium NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (additional_premium >= 0),
    CONSTRAINT uk_type_coverage UNIQUE (insurance_type_id, coverage_id)
    );

CREATE TABLE IF NOT EXISTS contract (
                                        id                BIGSERIAL PRIMARY KEY,
                                        contract_number   VARCHAR(50)   NOT NULL UNIQUE,
    client_id         BIGINT        NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    insurance_type_id BIGINT        NOT NULL REFERENCES insurance_type(id) ON DELETE RESTRICT,
    agent_id          BIGINT        REFERENCES app_user(id) ON DELETE SET NULL,
    start_date        DATE          NOT NULL,
    end_date          DATE          NOT NULL,
    premium_amount    NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (premium_amount >= 0),
    status            VARCHAR(20)   NOT NULL DEFAULT 'EN_ATTENTE'
    CHECK (status IN ('EN_ATTENTE', 'ACTIF', 'SUSPENDU', 'EXPIRE', 'RESILIE')),
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_contract_dates CHECK (end_date > start_date)
    );

CREATE TABLE IF NOT EXISTS contract_coverage (
                                                 id              BIGSERIAL PRIMARY KEY,
                                                 contract_id     BIGINT        NOT NULL REFERENCES contract(id) ON DELETE CASCADE,
    coverage_id     BIGINT        NOT NULL REFERENCES coverage(id) ON DELETE RESTRICT,
    applied_premium NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (applied_premium >= 0),
    CONSTRAINT uk_contract_coverage UNIQUE (contract_id, coverage_id)
    );

CREATE INDEX IF NOT EXISTS idx_contract_client ON contract(client_id);
CREATE INDEX IF NOT EXISTS idx_contract_status ON contract(status);