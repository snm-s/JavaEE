package fr.cytech.malinassur.dao;

import java.time.LocalDate;
import java.util.List;

import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.model.ContractStatus;

public class ContractDao extends BaseDao<Contract> {

    public ContractDao() {
        super(Contract.class);
    }

    /** Contrats d'un client (espace client). */
    public List<Contract> findByClient(Long clientId) {
        return read(s -> s.createQuery(
                "from Contract where clientId = :id order by createdAt desc", Contract.class)
                .setParameter("id", clientId)
                .getResultList());
    }

    /**
     * Recherche multicritère (consigne : "Rechercher un contrat").
     * Chaque critère est optionnel : null = on ne filtre pas dessus.
     */
    public List<Contract> search(String numberPart, Long clientId, Long typeId, ContractStatus status) {
        return read(s -> s.createQuery(
                "from Contract c where " +
                "(:num is null or lower(c.contractNumber) like :num) and " +
                "(:client is null or c.clientId = :client) and " +
                "(:type is null or c.insuranceType.id = :type) and " +
                "(:status is null or c.status = :status) " +
                "order by c.createdAt desc", Contract.class)
                .setParameter("num", numberPart == null || numberPart.isBlank()
                        ? null : "%" + numberPart.trim().toLowerCase() + "%")
                .setParameter("client", clientId)
                .setParameter("type", typeId)
                .setParameter("status", status)
                .getResultList());
    }

    /** Dernier numéro utilisé cette année, pour générer le suivant (CTR-2026-000043). */
    public long countCreatedInYear(int year) {
        return read(s -> s.createQuery(
                "select count(c) from Contract c where c.contractNumber like :prefix", Long.class)
                .setParameter("prefix", "CTR-" + year + "-%")
                .uniqueResult());
    }

    /** Passe en EXPIRE tous les contrats actifs dont la date de fin est dépassée. */
    public int expireOutdated(LocalDate today) {
        return inTransaction(s -> s.createMutationQuery(
                "update Contract set status = :expire where status = :actif and endDate < :today")
                .setParameter("expire", ContractStatus.EXPIRE)
                .setParameter("actif", ContractStatus.ACTIF)
                .setParameter("today", today)
                .executeUpdate());
    }
}
