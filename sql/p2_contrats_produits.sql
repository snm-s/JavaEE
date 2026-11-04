-- =====================================================================
-- Module Contrats & Produits (personne 2) — PostgreSQL
-- Tables conformes au diagramme du groupe + données de test.
-- ⚠️ Si la personne 1 a déjà créé ces tables dans son script, n'exécute
--    que la partie "DONNÉES DE TEST".
-- Prérequis : la table app_user existe (script de la personne 1).
-- =====================================================================

CREATE TABLE IF NOT EXISTS insurance_type (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    base_price  NUMERIC(10,2) NOT NULL CHECK (base_price > 0),
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS coverage (
    id            BIGSERIAL PRIMARY KEY,
    code          VARCHAR(30)  NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    coverage_type VARCHAR(50),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS insurance_type_coverage (
    id                 BIGSERIAL PRIMARY KEY,
    insurance_type_id  BIGINT NOT NULL REFERENCES insurance_type(id) ON DELETE CASCADE,
    coverage_id        BIGINT NOT NULL REFERENCES coverage(id),
    is_default         BOOLEAN NOT NULL DEFAULT FALSE,
    additional_premium NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (additional_premium >= 0),
    UNIQUE (insurance_type_id, coverage_id)
);

CREATE TABLE IF NOT EXISTS contract (
    id                BIGSERIAL PRIMARY KEY,
    contract_number   VARCHAR(30) NOT NULL UNIQUE,
    client_id         BIGINT NOT NULL REFERENCES app_user(id),
    insurance_type_id BIGINT NOT NULL REFERENCES insurance_type(id),
    agent_id          BIGINT REFERENCES app_user(id),
    start_date        DATE NOT NULL,
    end_date          DATE NOT NULL CHECK (end_date > start_date),
    premium_amount    NUMERIC(10,2) NOT NULL CHECK (premium_amount >= 0),
    status            VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE'
                      CHECK (status IN ('EN_ATTENTE','ACTIF','SUSPENDU','EXPIRE','RESILIE')),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS contract_coverage (
    id              BIGSERIAL PRIMARY KEY,
    contract_id     BIGINT NOT NULL REFERENCES contract(id) ON DELETE CASCADE,
    coverage_id     BIGINT NOT NULL REFERENCES coverage(id),
    applied_premium NUMERIC(10,2) NOT NULL DEFAULT 0,
    UNIQUE (contract_id, coverage_id)
);

-- =====================================================================
-- DONNÉES DE TEST
-- =====================================================================

INSERT INTO insurance_type (code, name, description, base_price) VALUES
 ('AUTO',  'Assurance automobile',    'Couverture de votre véhicule',             400.00),
 ('HABIT', 'Assurance habitation',    'Protection de votre logement',              180.00),
 ('SANTE', 'Assurance santé',         'Complémentaire santé',                      600.00),
 ('VOYAGE','Assurance voyage',        'Assistance et frais médicaux à l''étranger', 60.00),
 ('PRO',   'Assurance professionnelle','Responsabilité civile professionnelle',     900.00)
ON CONFLICT (code) DO NOTHING;

INSERT INTO coverage (code, name, description, coverage_type) VALUES
 ('RC',        'Responsabilité civile', 'Dommages causés à autrui',             'RESPONSABILITE'),
 ('VOL',       'Vol',                   'Vol du bien assuré',                   'DOMMAGE'),
 ('INCENDIE',  'Incendie',              'Dommages causés par le feu',           'DOMMAGE'),
 ('ACCIDENT',  'Accident',              'Dommages corporels et matériels',      'DOMMAGE'),
 ('DEGAT_EAUX','Dégâts des eaux',       'Fuites, infiltrations, inondations',   'DOMMAGE'),
 ('ASSISTANCE','Assistance',            'Dépannage, rapatriement, aide 24h/24', 'ASSISTANCE')
ON CONFLICT (code) DO NOTHING;

-- Catalogue : quelles garanties pour quel type, incluses ou en option, et à quel prix
INSERT INTO insurance_type_coverage (insurance_type_id, coverage_id, is_default, additional_premium)
SELECT t.id, c.id, v.is_default, v.premium
FROM (VALUES
  ('AUTO',  'RC',         TRUE,   0.00),
  ('AUTO',  'VOL',        FALSE, 120.00),
  ('AUTO',  'INCENDIE',   FALSE,  40.00),
  ('AUTO',  'ACCIDENT',   FALSE, 150.00),
  ('AUTO',  'ASSISTANCE', FALSE,  30.00),
  ('HABIT', 'RC',         TRUE,   0.00),
  ('HABIT', 'INCENDIE',   TRUE,   0.00),
  ('HABIT', 'DEGAT_EAUX', TRUE,   0.00),
  ('HABIT', 'VOL',        FALSE,  60.00),
  ('SANTE', 'ACCIDENT',   TRUE,   0.00),
  ('SANTE', 'ASSISTANCE', FALSE,  50.00),
  ('VOYAGE','ASSISTANCE', TRUE,   0.00),
  ('VOYAGE','VOL',        FALSE,  25.00),
  ('VOYAGE','ACCIDENT',   FALSE,  35.00),
  ('PRO',   'RC',         TRUE,   0.00),
  ('PRO',   'INCENDIE',   FALSE, 200.00),
  ('PRO',   'VOL',        FALSE, 150.00)
) AS v(type_code, cov_code, is_default, premium)
JOIN insurance_type t ON t.code = v.type_code
JOIN coverage c       ON c.code = v.cov_code
ON CONFLICT (insurance_type_id, coverage_id) DO NOTHING;
