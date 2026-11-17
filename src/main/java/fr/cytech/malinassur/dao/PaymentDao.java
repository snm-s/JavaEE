package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;
import fr.cytech.malinassur.model.Payment;
import fr.cytech.malinassur.model.PaymentStatus;

public class PaymentDao extends BaseDao<Payment> {

    public PaymentDao() {
        super(Payment.class);
    }

    @Override
    public List<Payment> findAll() {
        return read(session -> session.createQuery(
                        "SELECT p FROM Payment p JOIN FETCH p.contract ORDER BY p.dueDate DESC", Payment.class)
                .getResultList());
    }

    public Optional<Payment> findByReference(String reference) {
        return read(session -> session.createQuery(
                        "SELECT p FROM Payment p JOIN FETCH p.contract WHERE p.transactionReference = :ref", Payment.class)
                .setParameter("ref", reference)
                .uniqueResultOptional());
    }

    public List<Payment> findByContractId(Long contractId) {
        return read(session -> session.createQuery(
                        "SELECT p FROM Payment p JOIN FETCH p.contract WHERE p.contract.id = :contractId ORDER BY p.dueDate ASC", Payment.class)
                .setParameter("contractId", contractId)
                .getResultList());
    }

    public List<Payment> findByClientId(Long clientId) {
        return read(session -> session.createQuery(
                        "SELECT p FROM Payment p JOIN FETCH p.contract WHERE p.contract.clientId = :clientId ORDER BY p.dueDate DESC", Payment.class)
                .setParameter("clientId", clientId)
                .getResultList());
    }

    public List<Payment> findPendingPayments() {
        return read(session -> session.createQuery(
                        "SELECT p FROM Payment p JOIN FETCH p.contract WHERE p.status = :status ORDER BY p.dueDate ASC", Payment.class)
                .setParameter("status", PaymentStatus.EN_ATTENTE)
                .getResultList());
    }

    public long countCreatedInYear(int year) {
        return read(session -> session.createQuery(
                        "SELECT COUNT(p) FROM Payment p WHERE p.transactionReference LIKE :prefix", Long.class)
                .setParameter("prefix", "PAY-" + year + "-%")
                .uniqueResult());
    }
}