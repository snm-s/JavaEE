package fr.malinassur.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

/**
 * Garantie SOUSCRITE dans un contrat précis.
 * appliedPremium = copie du prix au moment de la souscription :
 * si l'admin change le tarif plus tard, les contrats déjà signés ne bougent pas.
 */
@Entity
@Table(name = "contract_coverage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id", "coverage_id"}))
public class ContractCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coverage_id")
    private Coverage coverage;

    @Column(name = "applied_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal appliedPremium = BigDecimal.ZERO;

    public ContractCoverage() {}

    public ContractCoverage(Contract contract, Coverage coverage, BigDecimal appliedPremium) {
        this.contract = contract;
        this.coverage = coverage;
        this.appliedPremium = appliedPremium;
    }

    // ----- Getters -----
    public Long getId() { return id; }
    public Contract getContract() { return contract; }
    public Coverage getCoverage() { return coverage; }
    public BigDecimal getAppliedPremium() { return appliedPremium; }
}
