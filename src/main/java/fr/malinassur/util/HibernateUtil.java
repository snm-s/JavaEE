package fr.malinassur.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Crée UNE seule SessionFactory pour toute l'application (elle est lourde à construire).
 * ⚠️ Si la personne 1 (Lead Tech) a déjà fait sa propre HibernateUtil, garde la sienne
 *    et supprime ce fichier : il en faut une seule dans le projet.
 */
public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY = build();

    private HibernateUtil() {}

    private static SessionFactory build() {
        try {
            // Lit src/main/resources/hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Impossible de créer la SessionFactory : " + e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }
}
