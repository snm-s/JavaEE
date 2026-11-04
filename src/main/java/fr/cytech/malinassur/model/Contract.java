package fr.cytech.malinassur.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private AppUser client;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "insurance_type_id")
    private InsuranceType insuranceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private AppUser agent;

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

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ContractCoverage> coverages = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public void addCoverage(Coverage coverage, BigDecimal appliedPremium) {
        coverages.add(new ContractCoverage(this, coverage, appliedPremium));
    }

    public void removeCoverage(Long coverageId) {
        coverages.removeIf(cc -> cc.getCoverage().getId().equals(coverageId));
    }

    public boolean hasCoverage(Long coverageId) {
        return coverages.stream().anyMatch(cc -> cc.getCoverage().getId().equals(coverageId));
    }

    public void recalculatePremium() {
        BigDecimal total = insuranceType.getBasePrice();
        for (ContractCoverage cc : coverages) {
            total = total.add(cc.getAppliedPremium());
        }
        this.premiumAmount = total;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
    public AppUser getClient() { return client; }
    public void setClient(AppUser client) { this.client = client; }
    public InsuranceType getInsuranceType() { return insuranceType; }
    public void setInsuranceType(InsuranceType insuranceType) { this.insuranceType = insuranceType; }
    public AppUser getAgent() { return agent; }
    public void setAgent(AppUser agent) { this.agent = agent; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getPremiumAmount() { return premiumAmount; }
    public void setPremiumAmount(BigDecimal premiumAmount) { this.premiumAmount = premiumAmount; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public List<ContractCoverage> getCoverages() { return coverages; }
}