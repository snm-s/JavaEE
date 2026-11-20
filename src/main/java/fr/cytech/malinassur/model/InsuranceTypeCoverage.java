package fr.cytech.malinassur.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

/**
 * Table de liaison "type d'assurance ↔ garantie" = le CATALOGUE.
 * Elle porte en plus deux infos : la garantie est-elle incluse d'office (isDefault)
 * et combien elle coûte en supplément (additionalPremium).
 */
@Entity
@Table(name = "insurance_type_coverage",
       uniqueConstraints = @UniqueConstraint(columnNames = {"insurance_type_id", "coverage_id"}))
public class InsuranceTypeCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_type_id")
    private InsuranceType insuranceType;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coverage_id")
    private Coverage coverage;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "additional_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal additionalPremium = BigDecimal.ZERO;

    public InsuranceTypeCoverage() {}

    public InsuranceTypeCoverage(InsuranceType type, Coverage coverage, boolean isDefault, BigDecimal additionalPremium) {
        this.insuranceType = type;
        this.coverage = coverage;
        this.isDefault = isDefault;
        this.additionalPremium = additionalPremium;
    }

    // ----- Getters / Setters -----
    public Long getId() { return id; }
    public InsuranceType getInsuranceType() { return insuranceType; }
    public Coverage getCoverage() { return coverage; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    public BigDecimal getAdditionalPremium() { return additionalPremium; }
    public void setAdditionalPremium(BigDecimal additionalPremium) { this.additionalPremium = additionalPremium; }
}
