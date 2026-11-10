package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;
import fr.cytech.malinassur.model.Payment;
import fr.cytech.malinassur.model.PaymentStatus;

public class PaymentDao extends BaseDao<Payment> {

    public PaymentDao() {
        super(Payment.class);
    }

    /** Recherche un paiement par sa référence unique de transaction. */
    public Optional<Payment> findByReference(String reference) {
        return read(session -> session.createQuery(
                        "FROM Payment p WHERE p.transactionReference = :ref", Payment.class)
                .setParameter("ref", reference)
                .uniqueResultOptional());
    }

    /** Liste l'historique des règlements et échéances d'un contrat. */
    public List<Payment> findByContractId(Long contractId) {
        return read(session -> session.createQuery(
                        "FROM Payment p WHERE p.contract.id = :contractId ORDER BY p.dueDate ASC", Payment.class)
                .setParameter("contractId", contractId)
                .getResultList());
    }

    /** Liste tous les paiements et échéances d'un client (espace client). */
    public List<Payment> findByClientId(Long clientId) {
        return read(session -> session.createQuery(
                "FROM Payment p WHERE p.contract.clientId = :clientId ORDER BY p.dueDate DESC", Payment.class)
                .setParameter("clientId", clientId)
                .getResultList());
    }

    /** Liste les paiements en attente de règlement (statut EN_ATTENTE). */
    public List<Payment> findPendingPayments() {
        return read(session -> session.createQuery(
                        "FROM Payment p WHERE p.status = :status ORDER BY p.dueDate ASC", Payment.class)
                .setParameter("status", PaymentStatus.EN_ATTENTE)
                .getResultList());
    }

    /** Compteur annuel pour générer le format PAY-YYYY-XXXXXX. */
    public long countCreatedInYear(int year) {
        return read(session -> session.createQuery(
                        "SELECT COUNT(p) FROM Payment p WHERE p.transactionReference LIKE :prefix", Long.class)
                .setParameter("prefix", "PAY-" + year + "-%")
                .uniqueResult());
    }
}