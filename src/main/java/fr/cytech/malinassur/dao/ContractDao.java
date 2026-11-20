package fr.cytech.malinassur.dao;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * La requête est construite morceau par morceau : PostgreSQL n'aime pas
     * les paramètres "null" du style (:x is null or ...).
     */
    public List<Contract> search(String numberPart, Long clientId, Long typeId, ContractStatus status) {
        StringBuilder hql = new StringBuilder("from Contract c where 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (numberPart != null && !numberPart.isBlank()) {
            hql.append(" and lower(c.contractNumber) like :num");
            params.put("num", "%" + numberPart.trim().toLowerCase() + "%");
        }
        if (clientId != null) {
            hql.append(" and c.clientId = :client");
            params.put("client", clientId);
        }
        if (typeId != null) {
            hql.append(" and c.insuranceType.id = :type");
            params.put("type", typeId);
        }
        if (status != null) {
            hql.append(" and c.status = :status");
            params.put("status", status);
        }
        hql.append(" order by c.createdAt desc");
        return read(s -> {
            var query = s.createQuery(hql.toString(), Contract.class);
            params.forEach((k, v) -> query.setParameter(k, v));
            return query.getResultList();
        });
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
