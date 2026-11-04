-- =====================================================================
-- ⚠️ FICHIER TEMPORAIRE, POUR TESTER SEULE
-- La table app_user appartient à la personne 1. Comme tu n'as pas encore
-- son script, on crée une version minimale (mêmes colonnes que le diagramme)
-- avec 2 utilisateurs de test. Quand le script de la personne 1 arrive,
-- tu supprimes ce fichier et tu utilises le sien.
-- À exécuter AVANT p2_contrats_produits.sql
-- =====================================================================

CREATE TABLE IF NOT EXISTS app_user (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL CHECK (role IN ('CLIENT','AGENT','ADMIN')),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO app_user (first_name, last_name, email, password_hash, role) VALUES
 ('Claire', 'Dupont', 'client.test@malinassur.fr', 'pas-encore-hache', 'CLIENT'),
 ('Marc',   'Martin', 'agent.test@malinassur.fr',  'pas-encore-hache', 'AGENT')
ON CONFLICT (email) DO NOTHING;
