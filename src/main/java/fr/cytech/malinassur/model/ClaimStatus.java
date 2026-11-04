package fr.cytech.malinassur.model;

/**
 * Statuts possibles d'un sinistre selon le cycle de vie métier.
 */
public enum ClaimStatus {
    DECLARE,
    EN_ANALYSE,
    ACCEPTE,
    REFUSE,
    INDEMNISE
}
