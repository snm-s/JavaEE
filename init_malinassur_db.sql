-- =====================================================================
-- APPLICATION MALIN'ASSUR - INITIALISATION COMPLÈTE BASE DE DONNÉES
-- Système : PostgreSQL (version 14+)
-- Base cible : malinassur_db
-- =====================================================================

-- Nettoyage complet préalable (ordre inverse des dépendances FK)
DROP TABLE IF EXISTS supporting_document CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS claim CASCADE;
DROP TABLE IF EXISTS contract_coverage CASCADE;
DROP TABLE IF EXISTS contract CASCADE;
DROP TABLE IF EXISTS insurance_type_coverage CASCADE;
DROP TABLE IF EXISTS coverage CASCADE;
DROP TABLE IF EXISTS insurance_type CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- =====================================================================
-- 1. MODULE SOCLE & UTILISATEURS (Dev 1)
-- =====================================================================
CREATE TABLE app_user (
                          id            BIGSERIAL PRIMARY KEY,
                          first_name    VARCHAR(100) NOT NULL,
                          last_name     VARCHAR(100) NOT NULL,
                          email         VARCHAR(255) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          role          VARCHAR(20)  NOT NULL CHECK (role IN ('CLIENT', 'AGENT', 'ADMIN')),
                          is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
                          created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_role  ON app_user(role);

-- =====================================================================
-- 2. MODULE CATALOGUE & CONTRATS (Dev 2)
-- =====================================================================
CREATE TABLE insurance_type (
                                id          BIGSERIAL PRIMARY KEY,
                                code        VARCHAR(20)   NOT NULL UNIQUE,
                                name        VARCHAR(100)  NOT NULL UNIQUE,
                                description TEXT,
                                base_price  NUMERIC(10,2) NOT NULL CHECK (base_price > 0),
                                is_active   BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE coverage (
                          id            BIGSERIAL PRIMARY KEY,
                          code          VARCHAR(30)  NOT NULL UNIQUE,
                          name          VARCHAR(100) NOT NULL,
                          description   TEXT,
                          coverage_type VARCHAR(50),
                          is_active     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE insurance_type_coverage (
                                         id                 BIGSERIAL PRIMARY KEY,
                                         insurance_type_id  BIGINT        NOT NULL REFERENCES insurance_type(id) ON DELETE CASCADE,
                                         coverage_id        BIGINT        NOT NULL REFERENCES coverage(id) ON DELETE RESTRICT,
                                         is_default         BOOLEAN       NOT NULL DEFAULT FALSE,
                                         additional_premium NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (additional_premium >= 0),
                                         CONSTRAINT uk_type_coverage UNIQUE (insurance_type_id, coverage_id)
);

CREATE TABLE contract (
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

CREATE TABLE contract_coverage (
                                   id              BIGSERIAL PRIMARY KEY,
                                   contract_id     BIGINT        NOT NULL REFERENCES contract(id) ON DELETE CASCADE,
                                   coverage_id     BIGINT        NOT NULL REFERENCES coverage(id) ON DELETE RESTRICT,
                                   applied_premium NUMERIC(10,2) NOT NULL DEFAULT 0.00 CHECK (applied_premium >= 0),
                                   CONSTRAINT uk_contract_coverage UNIQUE (contract_id, coverage_id)
);

CREATE INDEX idx_contract_client ON contract(client_id);
CREATE INDEX idx_contract_status ON contract(status);
CREATE INDEX idx_contract_number ON contract(contract_number);

-- =====================================================================
-- 3. MODULE SINISTRES & PAIEMENTS (Dev 3)
-- =====================================================================
CREATE TABLE claim (
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

CREATE TABLE supporting_document (
                                     id              BIGSERIAL PRIMARY KEY,
                                     file_name       VARCHAR(255) NOT NULL,
                                     storage_path    VARCHAR(500) NOT NULL,
                                     mime_type       VARCHAR(50),
                                     file_size_bytes BIGINT,
                                     uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                     claim_id        BIGINT       NOT NULL REFERENCES claim(id) ON DELETE CASCADE
);

CREATE TABLE payment (
                         id                    BIGSERIAL PRIMARY KEY,
                         transaction_reference VARCHAR(60)   NOT NULL UNIQUE,
                         amount                NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
                         due_date              DATE           NOT NULL,
                         payment_date          TIMESTAMPTZ,
                         payment_method        VARCHAR(30)    CHECK (payment_method IN ('CARTE', 'PRELEVEMENT', 'VIREMENT')),
                         status                VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE'
                             CHECK (status IN ('EN_ATTENTE', 'EFFECTUE', 'ECHOUE')),
                         contract_id           BIGINT         NOT NULL REFERENCES contract(id) ON DELETE RESTRICT,
                         created_at            TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_claim_contract ON claim(contract_id);
CREATE INDEX idx_claim_status   ON claim(status);
CREATE INDEX idx_payment_contract ON payment(contract_id);
CREATE INDEX idx_payment_status   ON payment(status);

-- =====================================================================
-- 4. JEU DE DONNÉES DE DÉMARRAGE COHÉRENT
-- =====================================================================

-- Utilisateurs (mot de passe initial pour tous : 'password123' ou hash BCrypt)
-- Hash BCrypt valide pour 'password123' : $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO app_user (first_name, last_name, email, password_hash, role) VALUES
                                                                             ('Admin', 'System', 'admin@malinassur.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
                                                                             ('Marc', 'Martin', 'agent.martin@malinassur.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT'),
                                                                             ('Sophie', 'Bernard', 'agent.bernard@malinassur.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT'),
                                                                             ('Claire', 'Dupont', 'claire.dupont@client.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT'),
                                                                             ('Thomas', 'Leroy', 'thomas.leroy@client.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT');

-- Types d'assurance
INSERT INTO insurance_type (code, name, description, base_price) VALUES
                                                                     ('AUTO',   'Assurance Automobile',     'Protection complète pour véhicule particulier ou professionnel', 400.00),
                                                                     ('HABIT',  'Assurance Habitation',     'Couverture multirisque pour résidence principale et locative',   180.00),
                                                                     ('SANTE',  'Complémentaire Santé',     'Remboursement des frais médicaux, dentaires et optiques',         600.00),
                                                                     ('VOYAGE', 'Assurance Voyage',         'Assistance médicale, rapatriement et annulation à l étranger',    60.00),
                                                                     ('PRO',    'Responsabilité Pro (RC)',  'Couverture des risques professionnels et pertes d exploitation', 900.00);

-- Garanties
INSERT INTO coverage (code, name, description, coverage_type) VALUES
                                                                  ('RC',         'Responsabilité Civile', 'Couvre les dommages causés aux tiers',             'RESPONSABILITE'),
                                                                  ('VOL',        'Vol & Vandalisme',      'Indemnisation en cas de vol ou détérioration',     'DOMMAGE'),
                                                                  ('INCENDIE',   'Incendie & Explosion',  'Prise en charge des dégâts liés au feu',           'DOMMAGE'),
                                                                  ('ACCIDENT',   'Tous Accidents',        'Dommages matériels et corporels tous accidents',   'DOMMAGE'),
                                                                  ('DEGAT_EAUX', 'Dégât des Eaux',        'Infiltrations, fuites de canalisation et ruptures','DOMMAGE'),
                                                                  ('ASSISTANCE', 'Assistance 24/7',       'Dépannage, remorquage et aide d urgence continue', 'ASSISTANCE');

-- Catalogue de garanties par type (incluses par défaut ou avec surprime)
INSERT INTO insurance_type_coverage (insurance_type_id, coverage_id, is_default, additional_premium)
SELECT t.id, c.id, v.is_def, v.supp
FROM (VALUES
          ('AUTO',   'RC',         TRUE,    0.00),
          ('AUTO',   'VOL',        FALSE, 120.00),
          ('AUTO',   'INCENDIE',   FALSE,  40.00),
          ('AUTO',   'ACCIDENT',   FALSE, 150.00),
          ('AUTO',   'ASSISTANCE', FALSE,  30.00),
          ('HABIT',  'RC',         TRUE,    0.00),
          ('HABIT',  'INCENDIE',   TRUE,    0.00),
          ('HABIT',  'DEGAT_EAUX', TRUE,    0.00),
          ('HABIT',  'VOL',        FALSE,  60.00),
          ('SANTE',  'ACCIDENT',   TRUE,    0.00),
          ('SANTE',  'ASSISTANCE', FALSE,  50.00),
          ('VOYAGE', 'ASSISTANCE', TRUE,    0.00),
          ('VOYAGE', 'VOL',        FALSE,  25.00),
          ('PRO',    'RC',         TRUE,    0.00),
          ('PRO',    'VOL',        FALSE, 150.00),
          ('PRO',    'INCENDIE',   FALSE, 200.00)
     ) AS v(type_code, cov_code, is_def, supp)
         JOIN insurance_type t ON t.code = v.type_code
         JOIN coverage c       ON c.code = v.cov_code;

-- Contrats de test
INSERT INTO contract (contract_number, client_id, insurance_type_id, agent_id, start_date, end_date, premium_amount, status)
SELECT
    'CTR-2026-000001',
    (SELECT id FROM app_user WHERE email = 'claire.dupont@client.fr'),
    (SELECT id FROM insurance_type WHERE code = 'AUTO'),
    (SELECT id FROM app_user WHERE email = 'agent.martin@malinassur.fr'),
    CURRENT_DATE - INTERVAL '60 days',
    CURRENT_DATE + INTERVAL '305 days',
    550.00,
    'ACTIF';

INSERT INTO contract (contract_number, client_id, insurance_type_id, agent_id, start_date, end_date, premium_amount, status)
SELECT
    'CTR-2026-000002',
    (SELECT id FROM app_user WHERE email = 'thomas.leroy@client.fr'),
    (SELECT id FROM insurance_type WHERE code = 'HABIT'),
    (SELECT id FROM app_user WHERE email = 'agent.bernard@malinassur.fr'),
    CURRENT_DATE - INTERVAL '10 days',
    CURRENT_DATE + INTERVAL '355 days',
    180.00,
    'ACTIF';

-- Garanties souscrites pour le contrat 1 (Auto de Claire Dupont : RC par défaut + VOL)
INSERT INTO contract_coverage (contract_id, coverage_id, applied_premium)
SELECT c.id, cov.id, 0.00
FROM contract c, coverage cov
WHERE c.contract_number = 'CTR-2026-000001' AND cov.code = 'RC';

INSERT INTO contract_coverage (contract_id, coverage_id, applied_premium)
SELECT c.id, cov.id, 120.00
FROM contract c, coverage cov
WHERE c.contract_number = 'CTR-2026-000001' AND cov.code = 'VOL';

-- Sinistre de test rattaché au contrat 1
INSERT INTO claim (claim_number, incident_date, declaration_date, claim_type, description, estimated_amount, status, contract_id, agent_id)
SELECT
    'SIN-2026-000001',
    CURRENT_DATE - INTERVAL '5 days',
    NOW() - INTERVAL '4 days',
    'Accident matériel',
    'Choc arrière sur parking de supermarché avec véhicule identifié.',
    850.00,
    'DECLARE',
    c.id,
    NULL
FROM contract c WHERE c.contract_number = 'CTR-2026-000001';

-- Pièce justificative attachée au sinistre 1
INSERT INTO supporting_document (file_name, storage_path, mime_type, file_size_bytes, claim_id)
SELECT
    'constat_amiable.pdf',
    'uploads/claims/constat_amiable_CTR001.pdf',
    'application/pdf',
    245890,
    cl.id
FROM claim cl WHERE cl.claim_number = 'SIN-2026-000001';

-- Échéances et paiements de test
INSERT INTO payment (transaction_reference, amount, due_date, payment_date, payment_method, status, contract_id)
SELECT
    'PAY-2026-000001',
    550.00,
    CURRENT_DATE - INTERVAL '60 days',
    NOW() - INTERVAL '59 days',
    'CARTE',
    'EFFECTUE',
    c.id
FROM contract c WHERE c.contract_number = 'CTR-2026-000001';

INSERT INTO payment (transaction_reference, amount, due_date, payment_date, payment_method, status, contract_id)
SELECT
    'PAY-2026-000002',
    180.00,
    CURRENT_DATE + INTERVAL '10 days',
    NULL,
    'PRELEVEMENT',
    'EN_ATTENTE',
    c.id
FROM contract c WHERE c.contract_number = 'CTR-2026-000002';
