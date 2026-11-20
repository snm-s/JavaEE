package fr.cytech.malinassur.model;

import java.util.Locale;

/**
 * Rôles disponibles dans l'application.
 *
 * <p>Le schéma PostgreSQL stocke le rôle dans une colonne scalaire
 * {@code app_user.role VARCHAR(20)} avec une contrainte {@code CHECK} : un utilisateur
 * possède donc exactement un rôle. Cette énumération est le modèle Role côté Java ;
 * elle remplace les chaînes libres ("CLIENT", "AGENT", "ADMIN") qui pouvaient contenir
 * des fautes de frappe.</p>
 */
public enum UserRole {

    CLIENT("Client", "/client/contracts"),
    AGENT("Agent d'assurance", "/agent/contracts"),
    ADMIN("Administrateur", "/admin/contracts");

    private final String label;
    private final String homePath;

    UserRole(String label, String homePath) {
        this.label = label;
        this.homePath = homePath;
    }

    /** Libellé affichable dans une vue. */
    public String getLabel() {
        return label;
    }

    /** Page d'accueil de l'espace correspondant au rôle. */
    public String getHomePath() {
        return homePath;
    }

    /**
     * Indique si le rôle autorise l'accès au chemin demandé.
     * Le chemin doit commencer par "/" ; la chaîne de requête est ignorée.
     */
    public boolean canAccess(String path) {
        if (path == null || !path.startsWith("/")) return false;
        String clean = path;
        int query = clean.indexOf('?');
        if (query >= 0) clean = clean.substring(0, query);

        return switch (this) {
            case ADMIN -> clean.equals("/admin") || clean.startsWith("/admin/");
            case AGENT -> clean.equals("/agent") || clean.startsWith("/agent/");
            case CLIENT -> clean.equals("/client") || clean.startsWith("/client/");
        };
    }

    /**
     * Convertit une valeur de la base (ou d'un ancien code) en rôle typé.
     *
     * @throws IllegalArgumentException si la valeur est absente ou inconnue
     */
    public static UserRole fromDatabase(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Rôle utilisateur absent.");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rôle utilisateur inconnu : " + value);
        }
    }
}
