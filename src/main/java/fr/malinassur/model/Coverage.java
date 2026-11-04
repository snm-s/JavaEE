package fr.malinassur.model;

import jakarta.persistence.*;

/**
 * Garantie (responsabilité civile, vol, incendie, accident, dégâts des eaux, assistance...).
 * Une garantie existe une seule fois et peut être proposée par plusieurs types d'assurance.
 */
@Entity
@Table(name = "coverage")
public class Coverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;              // ex : "VOL"

    @Column(nullable = false)
    private String name;              // ex : "Vol"

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "coverage_type")
    private String coverageType;      // catégorie libre : "DOMMAGE", "RESPONSABILITE", "ASSISTANCE"...

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Coverage() {}

    public Coverage(String code, String name, String description, String coverageType) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.coverageType = coverageType;
    }

    // ----- Getters / Setters -----
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverageType() { return coverageType; }
    public void setCoverageType(String coverageType) { this.coverageType = coverageType; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
