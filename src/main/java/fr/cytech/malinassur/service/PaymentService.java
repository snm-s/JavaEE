package fr.cytech.malinassur.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

import fr.cytech.malinassur.dao.ContractDao;
import fr.cytech.malinassur.dao.PaymentDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.model.ContractStatus;
import fr.cytech.malinassur.model.Payment;
import fr.cytech.malinassur.model.PaymentMethod;
import fr.cytech.malinassur.model.PaymentStatus;

public class PaymentService {

    private final PaymentDao paymentDao = new PaymentDao();
    private final ContractDao contractDao = new ContractDao();

    /** Génère une nouvelle échéance à payer (statut EN_ATTENTE) sur un contrat. */
    public Payment createSchedule(Long contractId, BigDecimal amount, LocalDate dueDate) {
        if (contractId == null) throw new BusinessException("Le contrat est obligatoire.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le montant de l'échéance doit être strictement positif.");
        }
        if (dueDate == null) throw new BusinessException("La date d'échéance est obligatoire.");

        Contract contract = contractDao.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable."));

        if (contract.getStatus() == ContractStatus.RESILIE) {
            throw new BusinessException("Impossible de créer une échéance sur un contrat résilié.");
        }

        Payment payment = new Payment(nextTransactionReference(), amount, dueDate, contract);
        return paymentDao.save(payment);
    }

    /** Enregistre un paiement immédiat effectué directement (par carte, virement ou prélèvement). */
    public Payment recordDirectPayment(Long contractId, BigDecimal amount, PaymentMethod method) {
        if (contractId == null) throw new BusinessException("Le contrat est obligatoire.");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le montant payé doit être strictement positif.");
        }
        if (method == null) throw new BusinessException("Le moyen de paiement est obligatoire.");

        Contract contract = contractDao.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable."));

        Payment payment = new Payment(nextTransactionReference(), amount, LocalDate.now(), contract);
        payment.setPaymentMethod(method);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.EFFECTUE);

        return paymentDao.save(payment);
    }

    /** Règle une échéance existante qui était en attente. */
    public Payment payInstallment(Long paymentId, Long clientId, PaymentMethod method) {
        if (method == null) throw new BusinessException("Veuillez choisir un moyen de paiement.");

        Payment payment = getById(paymentId);

        // Si appelé par un client, vérifier que le contrat lui appartient
        if (clientId != null && !payment.getContract().getClientId().equals(clientId)) {
            throw new BusinessException("Vous n'êtes pas autorisé à régler cette échéance.");
        }
        if (payment.getStatus() == PaymentStatus.EFFECTUE) {
            throw new BusinessException("Cette échéance a déjà été réglée.");
        }

        payment.setPaymentMethod(method);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.EFFECTUE);

        return paymentDao.update(payment);
    }

    /** Marque un prélèvement ou règlement comme échoué (côté agent/système). */
    public Payment markAsFailed(Long paymentId) {
        Payment payment = getById(paymentId);
        if (payment.getStatus() == PaymentStatus.EFFECTUE) {
            throw new BusinessException("Un paiement déjà effectué ne peut pas passer en échec.");
        }
        payment.setStatus(PaymentStatus.ECHOUE);
        return paymentDao.update(payment);
    }

    // =====================================================================
    // CONSULTATION
    // =====================================================================

    public Payment getById(Long id) {
        return paymentDao.findById(id)
                .orElseThrow(() -> new BusinessException("Paiement introuvable."));
    }

    public List<Payment> findByClient(Long clientId) {
        return paymentDao.findByClientId(clientId);
    }

    public List<Payment> findByContract(Long contractId) {
        return paymentDao.findByContractId(contractId);
    }

    public List<Payment> findPending() {
        return paymentDao.findPendingPayments();
    }

    public List<Payment> findAll() {
        return paymentDao.findAll();
    }

    private String nextTransactionReference() {
        int year = Year.now().getValue();
        long next = paymentDao.countCreatedInYear(year) + 1;
        return String.format("PAY-%d-%06d", year, next);
    }
}