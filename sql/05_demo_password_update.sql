-- =====================================================================
-- 05_demo_password_update.sql
-- Corrige le hash des comptes de démonstration sur une base DÉJÀ initialisée.
--
-- Mot de passe commun aux comptes de démonstration : password123
-- Hash BCrypt (coût 10) correspondant, vérifié avec jBCrypt 0.4.
--
-- Ce script est idempotent : il ne touche qu'aux cinq comptes de seed.
-- Les scripts 01 à 04 créent le schéma ; celui-ci ne fait que réparer la DML.
-- =====================================================================

UPDATE app_user
SET password_hash = '$2a$10$95OH7KWpN6KlNOUa2ETfWuBkpwLHcC71ZR1Ng3N29dXjIeYPP3KgC'
WHERE email IN (
    'admin@malinassur.fr',
    'agent.martin@malinassur.fr',
    'agent.bernard@malinassur.fr',
    'claire.dupont@client.fr',
    'thomas.leroy@client.fr'
);
