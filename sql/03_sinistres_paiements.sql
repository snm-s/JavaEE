-- =====================================================================
-- 03_sinistres_paiements.sql (Module 3)
-- Sinistres, pièces justificatives et échéances de paiement
-- =====================================================================

CREATE TABLE IF NOT EXISTS claim (
                                     id               BIGSERIAL PRIMARY KEY,
                                     claim_number     VARCHAR(50)    NOT NULL UNIQUE,
    incident_date    DATE           NOT NULL,
    declaration_date TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    claim_type       VARCHAR(100)   NOT NULL,
    description      TEXT           NOT NULL,
    estimated_amount NUMERIC(12, 2) NOT NULL CHECK (estimated_amount >= 0),
    indemnity_amount NUMERIC(12, 2) CHECK (indemnity_amount >= 0),
    agent_comment    TEXT,
    status           VARCHAR(20)    NOT NULL DEFAULT 'DECLARE'
    CHECK (status IN ('DECLARE', 'EN_ANALYSE', 'ACCEPTE', 'REFUSE', 'INDEMNISE')),
    contract_id      BIGINT         NOT NULL REFERENCES contract(id) ON DELETE RESTRICT,
    agent_id         BIGINT         REFERENCES app_user(id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS supporting_document (
                                                   id              BIGSERIAL PRIMARY KEY,
                                                   file_name       VARCHAR(255) NOT NULL,
    storage_path    VARCHAR(500) NOT NULL,
    mime_type       VARCHAR(50),
    file_size_bytes BIGINT,
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    claim_id        BIGINT       NOT NULL REFERENCES claim(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS payment (
                                       id                    BIGSERIAL PRIMARY KEY,
                                       transaction_reference VARCHAR(60)    NOT NULL UNIQUE,
    amount                NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    due_date              DATE           NOT NULL,
    payment_date          TIMESTAMPTZ,
    payment_method        VARCHAR(30)    CHECK (payment_method IN ('CARTE', 'PRELEVEMENT', 'VIREMENT')),
    status                VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE'
    CHECK (status IN ('EN_ATTENTE', 'EFFECTUE', 'ECHOUE')),
    contract_id           BIGINT         NOT NULL REFERENCES contract(id) ON DELETE RESTRICT,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_claim_contract ON claim(contract_id);
CREATE INDEX IF NOT EXISTS idx_claim_status   ON claim(status);
CREATE INDEX IF NOT EXISTS idx_payment_contract ON payment(contract_id);