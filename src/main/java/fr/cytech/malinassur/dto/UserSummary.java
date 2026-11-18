package fr.cytech.malinassur.dto;

/**
 * Résumé d'un utilisateur (id, nom, email) pour l'affichage dans les pages contrats.
 * On ne dépend pas de la classe AppUser (module 1, encore en cours) : on lit
 * directement les colonnes de la table app_user.
 */
public class UserSummary {

    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;

    public UserSummary(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }

    /** Utilisable dans les JSP : ${client.fullName} */
    public String getFullName() { return firstName + " " + lastName; }
}
