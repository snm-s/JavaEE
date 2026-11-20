package fr.cytech.malinassur.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * Contrat d'assurance : relie UN client, UN type d'assurance et PLUSIEURS garanties.
 *
 * client_id et agent_id pointent vers app_user (table de la personne 1).
 * On les garde en simples Long pour que notre module ne dépende pas de sa classe Java :
 * chacun peut avancer de son côté. (La clé étrangère existe quand même en base.)
 */
@Entity
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;    // ex : "CTR-2026-000042"

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "insurance_type_id")
    private InsuranceType insuranceType;

    @Column(name = "agent_id")        // nullable : assigné après création
    private Long agentId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "premium_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal premiumAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status = ContractStatus.EN_ATTENTE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Les garanties choisies pour CE contrat (table contract_coverage). */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ContractCoverage> coverages = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    // ----- Méthodes métier simples (la logique complète est dans ContractService) -----

    public void addCoverage(Coverage coverage, BigDecimal appliedPremium) {
        coverages.add(new ContractCoverage(this, coverage, appliedPremium));
    }

    public void removeCoverage(Long coverageId) {
        coverages.removeIf(cc -> cc.getCoverage().getId().equals(coverageId));
    }

    public boolean hasCoverage(Long coverageId) {
        return coverages.stream().anyMatch(cc -> cc.getCoverage().getId().equals(coverageId));
    }

    /** Prime = prix de base du type + somme des suppléments des garanties choisies. */
    public void recalculatePremium() {
        BigDecimal total = insuranceType.getBasePrice();
        for (ContractCoverage cc : coverages) {
            total = total.add(cc.getAppliedPremium());
        }
        this.premiumAmount = total;
    }

    // ----- Getters / Setters -----
    public Long getId() { return id; }
    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public InsuranceType getInsuranceType() { return insuranceType; }
    public void setInsuranceType(InsuranceType insuranceType) { this.insuranceType = insuranceType; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long agentId) { this.agentId = agentId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<ContractCoverage> getCoverages() { return coverages; }
}
