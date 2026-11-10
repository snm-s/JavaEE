package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;
import fr.cytech.malinassur.model.Claim;
import fr.cytech.malinassur.model.ClaimStatus;

public class ClaimDao extends BaseDao<Claim> {

    public ClaimDao() {
        super(Claim.class);
    }

    /** Recherche un sinistre par son numéro unique (ex : SIN-2026-000001). */
    public Optional<Claim> findByClaimNumber(String claimNumber) {
        return read(session -> session.createQuery(
                        "FROM Claim c WHERE c.claimNumber = :num", Claim.class)
                .setParameter("num", claimNumber)
                .uniqueResultOptional());
    }

    /** Liste tous les sinistres rattachés à un contrat spécifique. */
    public List<Claim> findByContractId(Long contractId) {
        return read(session -> session.createQuery(
                        "FROM Claim c WHERE c.contract.id = :contractId ORDER BY c.declarationDate DESC", Claim.class)
                .setParameter("contractId", contractId)
                .getResultList());
    }

    /** Liste tous les sinistres d'un client (espace client). */
    public List<Claim> findByClientId(Long clientId) {
        return read(session -> session.createQuery(
                        "FROM Claim c WHERE c.contract.clientId = :clientId ORDER BY c.declarationDate DESC", Claim.class)
                .setParameter("clientId", clientId)
                .getResultList());
    }

    /** Liste les sinistres filtrés par statut (pour le tableau de bord agent). */
    public List<Claim> findByStatus(ClaimStatus status) {
        return read(session -> session.createQuery(
                        "FROM Claim c WHERE c.status = :status ORDER BY c.declarationDate ASC", Claim.class)
                .setParameter("status", status)
                .getResultList());
    }

    /** Charge le sinistre avec ses pièces justificatives (évite LazyInitializationException). */
    public Optional<Claim> findWithDocuments(Long id) {
        return read(session -> session.createQuery(
                        "SELECT DISTINCT c FROM Claim c LEFT JOIN FETCH c.supportingDocuments WHERE c.id = :id", Claim.class)
                .setParameter("id", id)
                .uniqueResultOptional());
    }

    /** Compteur annuel pour générer le format SIN-YYYY-XXXXXX. */
    public long countCreatedInYear(int year) {
        return read(session -> session.createQuery(
                        "SELECT COUNT(c) FROM Claim c WHERE c.claimNumber LIKE :prefix", Long.class)
                .setParameter("prefix", "SIN-" + year + "-%")
                .uniqueResult());
    }
}