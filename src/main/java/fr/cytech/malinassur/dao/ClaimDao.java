package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;
import fr.cytech.malinassur.model.Claim;
import fr.cytech.malinassur.model.ClaimStatus;

public class ClaimDao extends BaseDao<Claim> {

    public ClaimDao() {
        super(Claim.class);
    }

    @Override
    public List<Claim> findAll() {
        return read(session -> session.createQuery(
                        "SELECT c FROM Claim c JOIN FETCH c.contract ORDER BY c.declarationDate DESC", Claim.class)
                .getResultList());
    }

    public Optional<Claim> findByClaimNumber(String claimNumber) {
        return read(session -> session.createQuery(
                        "SELECT c FROM Claim c JOIN FETCH c.contract WHERE c.claimNumber = :num", Claim.class)
                .setParameter("num", claimNumber)
                .uniqueResultOptional());
    }

    public List<Claim> findByContractId(Long contractId) {
        return read(session -> session.createQuery(
                        "SELECT c FROM Claim c JOIN FETCH c.contract WHERE c.contract.id = :contractId ORDER BY c.declarationDate DESC", Claim.class)
                .setParameter("contractId", contractId)
                .getResultList());
    }

    public List<Claim> findByClientId(Long clientId) {
        return read(session -> session.createQuery(
                        "SELECT c FROM Claim c JOIN FETCH c.contract WHERE c.contract.clientId = :clientId ORDER BY c.declarationDate DESC", Claim.class)
                .setParameter("clientId", clientId)
                .getResultList());
    }

    public List<Claim> findByStatus(ClaimStatus status) {
        return read(session -> session.createQuery(
                        "SELECT c FROM Claim c JOIN FETCH c.contract WHERE c.status = :status ORDER BY c.declarationDate ASC", Claim.class)
                .setParameter("status", status)
                .getResultList());
    }

    public Optional<Claim> findWithDocuments(Long id) {
        return read(session -> session.createQuery(
                        "SELECT DISTINCT c FROM Claim c JOIN FETCH c.contract LEFT JOIN FETCH c.supportingDocuments WHERE c.id = :id", Claim.class)
                .setParameter("id", id)
                .uniqueResultOptional());
    }

    public long countCreatedInYear(int year) {
        return read(session -> session.createQuery(
                        "SELECT COUNT(c) FROM Claim c WHERE c.claimNumber LIKE :prefix", Long.class)
                .setParameter("prefix", "SIN-" + year + "-%")
                .uniqueResult());
    }
}