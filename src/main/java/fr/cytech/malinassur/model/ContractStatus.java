package fr.cytech.malinassur.model;

/**
 * Statuts possibles d'un contrat (imposés par les consignes).
 * Stockés en texte dans la colonne contract.status grâce à @Enumerated(EnumType.STRING).
 */
public enum ContractStatus {
    EN_ATTENTE,
    ACTIF,
    SUSPENDU,
    EXPIRE,
    RESILIE
}
