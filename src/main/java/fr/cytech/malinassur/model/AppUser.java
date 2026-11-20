package fr.cytech.malinassur.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Utilisateur de la plateforme (client, agent ou administrateur).
 *
 * <p>Correspond à la table {@code app_user} du schéma PostgreSQL. Le rôle est
 * stocké en base sous forme de chaîne et converti par {@link UserRole}. Le hash du
 * mot de passe n'est jamais exposé dans les vues : seule la couche service
 * l'utilise pour vérifier une authentification.</p>
 */
@Entity
@Table(name = "app_user")
public class AppUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Constructeur sans argument requis par JPA / Hibernate. */
    public AppUser() {
    }

    /** Constructeur métier utilisé à l'inscription et par les jeux de données. */
    public AppUser(String firstName, String lastName, String email, String passwordHash, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = true;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (role == null) {
            role = UserRole.CLIENT;
        }
    }

    /** Nom complet utilisable directement dans les JSP : ${user.fullName}. */
    public String getFullName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        return (first + " " + last).trim();
    }

    public boolean isClient() {
        return role == UserRole.CLIENT;
    }

    public boolean isAgent() {
        return role == UserRole.AGENT;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    // ----- Getters / Setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Deux utilisateurs sont égaux s'ils ont le même e-mail (colonne unique en base).
     * L'identifiant est utilisé lorsqu'il est déjà connu.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUser other)) return false;
        if (id != null && other.id != null) return id.equals(other.id);
        return email != null && email.equalsIgnoreCase(other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email == null ? null : email.toLowerCase(Locale.ROOT));
    }

    @Override
    public String toString() {
        return "AppUser{id=" + id + ", email='" + email + "', role=" + role + "}";
    }
}
