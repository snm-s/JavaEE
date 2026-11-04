package fr.cytech.assurance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "declaration_date", nullable = false)
    private LocalDateTime declarationDate;

    @Column(name = "claim_type", nullable = false, length = 100)
    private String claimType;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "estimated_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal estimatedAmount;

    // Reste null tant que l'agent n'a pas validé/indemnisé
    @Column(name = "indemnity_amount", precision = 12, scale = 2)
    private BigDecimal indemnityAmount;

    // Reste null à la déclaration initiale du client
    @Column(name = "agent_comment", columnDefinition = "TEXT")
    private String agentComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClaimStatus status;

    // Contrat concerné par le sinistre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // Agent en charge de l'analyse (null à la déclaration)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private AppUser agent;

    // Pièces justificatives rattachées au dossier
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportingDocument> supportingDocuments = new ArrayList<>();

    // Constructeur sans argument requis par JPA / Hibernate
    public Claim() {
        this.declarationDate = LocalDateTime.now();
        this.status = ClaimStatus.DECLARE;
    }

    // Constructeur métier pour initialiser une déclaration client
    public Claim(String claimNumber, LocalDate incidentDate, String claimType,
                 String description, BigDecimal estimatedAmount, Contract contract) {
        this();
        this.claimNumber = claimNumber;
        this.incidentDate = incidentDate;
        this.claimType = claimType;
        this.description = description;
        this.estimatedAmount = estimatedAmount;
        this.contract = contract;
    }

    // Méthodes d'aide pour maintenir la cohérence bidirectionnelle des documents
    public void addDocument(SupportingDocument document) {
        supportingDocuments.add(document);
        document.setClaim(this);
    }

    public void removeDocument(SupportingDocument document) {
        supportingDocuments.remove(document);
        document.setClaim(null);
    }

    // --- Getters et Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public LocalDateTime getDeclarationDate() {
        return declarationDate;
    }

    public void setDeclarationDate(LocalDateTime declarationDate) {
        this.declarationDate = declarationDate;
    }

    public String getClaimType() {
        return claimType;
    }

    public void setClaimType(String claimType) {
        this.claimType = claimType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public BigDecimal getIndemnityAmount() {
        return indemnityAmount;
    }

    public void setIndemnityAmount(BigDecimal indemnityAmount) {
        this.indemnityAmount = indemnityAmount;
    }

    public String getAgentComment() {
        return agentComment;
    }

    public void setAgentComment(String agentComment) {
        this.agentComment = agentComment;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public AppUser getAgent() {
        return agent;
    }

    public void setAgent(AppUser agent) {
        this.agent = agent;
    }

    public List<SupportingDocument> getSupportingDocuments() {
        return supportingDocuments;
    }

    public void setSupportingDocuments(List<SupportingDocument> supportingDocuments) {
        this.supportingDocuments = supportingDocuments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Claim claim)) return false;
        return Objects.equals(claimNumber, claim.claimNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claimNumber);
    }
}