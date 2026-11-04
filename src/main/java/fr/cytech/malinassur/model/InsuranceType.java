package fr.malinassur.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

/**
 * Type d'assurance (Auto, Habitation, Santé, Voyage, Pro...).
 * Les types sont des LIGNES en base, pas des sous-classes Java :
 * ajouter un nouveau type = insérer une ligne, sans modifier le code (consigne 2.3).
 */
@Entity
@Table(name = "insurance_type")
public class InsuranceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;              // ex : "AUTO"

    @Column(nullable = false, unique = true)
    private String name;              // ex : "Assurance automobile"

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Le "menu" des garanties proposées par ce type (table insurance_type_coverage). */
    @OneToMany(mappedBy = "insuranceType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InsuranceTypeCoverage> offeredCoverages = new ArrayList<>();

    public InsuranceType() {}

    public InsuranceType(String code, String name, String description, BigDecimal basePrice) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
    }

    /** Ajoute une garantie au catalogue de ce type, en gardant les deux côtés de la relation cohérents. */
    public InsuranceTypeCoverage offer(Coverage coverage, boolean isDefault, BigDecimal additionalPremium) {
        InsuranceTypeCoverage itc = new InsuranceTypeCoverage(this, coverage, isDefault, additionalPremium);
        offeredCoverages.add(itc);
        return itc;
    }

    // ----- Getters / Setters -----
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public List<InsuranceTypeCoverage> getOfferedCoverages() { return offeredCoverages; }
}
