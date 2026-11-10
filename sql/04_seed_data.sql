-- =====================================================================
-- 04_seed_data.sql : Données de démarrage
-- =====================================================================

-- 1. Utilisateurs
INSERT INTO app_user (first_name, last_name, email, password_hash, role) VALUES
                                                                             ('Admin', 'System', 'admin@malinassur.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN'),
                                                                             ('Marc', 'Martin', 'agent.martin@malinassur.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT'),
                                                                             ('Sophie', 'Bernard', 'agent.bernard@malinassur.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'AGENT'),
                                                                             ('Claire', 'Dupont', 'claire.dupont@client.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT'),
                                                                             ('Thomas', 'Leroy', 'thomas.leroy@client.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT')
    ON CONFLICT (email) DO NOTHING;

-- 2. Types d'assurance
INSERT INTO insurance_type (code, name, description, base_price) VALUES
                                                                     ('AUTO',   'Assurance Automobile',     'Protection véhicule personnel ou pro', 400.00),
                                                                     ('HABIT',  'Assurance Habitation',     'Protection logement et biens',          180.00),
                                                                     ('SANTE',  'Complémentaire Santé',     'Frais médicaux et hospitaliers',        600.00),
                                                                     ('VOYAGE', 'Assurance Voyage',         'Assistance et frais à l étranger',       60.00),
                                                                     ('PRO',    'Responsabilité Pro (RC)',  'Couverture civile des professionnels',   900.00)
    ON CONFLICT (code) DO NOTHING;

-- 3. Garanties
INSERT INTO coverage (code, name, description, coverage_type) VALUES
                                                                  ('RC',         'Responsabilité Civile', 'Dommages causés aux tiers',             'RESPONSABILITE'),
                                                                  ('VOL',        'Vol & Vandalisme',      'Indemnisation vol ou effraction',       'DOMMAGE'),
                                                                  ('INCENDIE',   'Incendie',              'Dommages causés par le feu',            'DOMMAGE'),
                                                                  ('ACCIDENT',   'Tous Accidents',        'Dommages matériels et corporels',       'DOMMAGE'),
                                                                  ('DEGAT_EAUX', 'Dégât des Eaux',        'Inondations, ruptures de canalisation', 'DOMMAGE'),
                                                                  ('ASSISTANCE', 'Assistance 24/7',       'Dépannage, remorquage d urgence',       'ASSISTANCE')
    ON CONFLICT (code) DO NOTHING;

-- 4. Catalogue des formules (relations type <-> garantie)
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
         JOIN coverage c       ON c.code = v.cov_code
    ON CONFLICT (insurance_type_id, coverage_id) DO NOTHING;

-- 5. Contrat actif de test
INSERT INTO contract (contract_number, client_id, insurance_type_id, agent_id, start_date, end_date, premium_amount, status)
SELECT
    'CTR-2026-000001',
    (SELECT id FROM app_user WHERE email = 'claire.dupont@client.fr'),
    (SELECT id FROM insurance_type WHERE code = 'AUTO'),
    (SELECT id FROM app_user WHERE email = 'agent.martin@malinassur.fr'),
    CURRENT_DATE - INTERVAL '60 days',
    CURRENT_DATE + INTERVAL '305 days',
    520.00,
    'ACTIF'
ON CONFLICT (contract_number) DO NOTHING;

-- 6. Garanties rattachées au contrat
INSERT INTO contract_coverage (contract_id, coverage_id, applied_premium)
SELECT c.id, cov.id, 0.00
FROM contract c, coverage cov
WHERE c.contract_number = 'CTR-2026-000001' AND cov.code = 'RC'
    ON CONFLICT (contract_id, coverage_id) DO NOTHING;

INSERT INTO contract_coverage (contract_id, coverage_id, applied_premium)
SELECT c.id, cov.id, 120.00
FROM contract c, coverage cov
WHERE c.contract_number = 'CTR-2026-000001' AND cov.code = 'VOL'
    ON CONFLICT (contract_id, coverage_id) DO NOTHING;

-- 7. Sinistre déclaré de test
INSERT INTO claim (claim_number, incident_date, declaration_date, claim_type, description, estimated_amount, status, contract_id)
SELECT
    'SIN-2026-000001',
    CURRENT_DATE - INTERVAL '5 days',
    NOW() - INTERVAL '4 days',
    'Accident matériel',
    'Choc arrière sur parking avec tiers identifié.',
    850.00,
    'DECLARE',
    c.id
FROM contract c WHERE c.contract_number = 'CTR-2026-000001'
ON CONFLICT (claim_number) DO NOTHING;

-- 8. Échéance de paiement
INSERT INTO payment (transaction_reference, amount, due_date, payment_date, payment_method, status, contract_id)
SELECT
    'PAY-2026-000001',
    520.00,
    CURRENT_DATE - INTERVAL '60 days',
    NOW() - INTERVAL '59 days',
    'CARTE',
    'EFFECTUE',
    c.id
FROM contract c WHERE c.contract_number = 'CTR-2026-000001'
ON CONFLICT (transaction_reference) DO NOTHING;