package fr.cytech.malinassur.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import fr.cytech.malinassur.dto.UserSummary;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

/**
 * Accès aux utilisateurs ({@code app_user}).
 *
 * <p>Ce DAO remplace {@code UserLookupDao} : une seule classe gère désormais la
 * lecture et l'écriture des utilisateurs, avec des requêtes HQL paramétrées
 * (protection contre l'injection SQL).</p>
 */
public class UserDao extends BaseDao<AppUser> {

    private static final String SUMMARY_SELECT =
            "select new fr.cytech.malinassur.dto.UserSummary(u.id, u.firstName, u.lastName, u.email) "
                    + "from AppUser u ";

    public UserDao() {
        super(AppUser.class);
    }

    /** Recherche insensible à la casse sur l'e-mail (colonne unique). */
    public Optional<AppUser> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return read(session -> session.createQuery(
                        "from AppUser u where lower(u.email) = :email", AppUser.class)
                .setParameter("email", normalized)
                .setMaxResults(1)
                .uniqueResultOptional());
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    /** Utilisateurs actifs d'un rôle, triés par nom puis prénom. */
    public List<AppUser> findActiveByRole(UserRole role) {
        return read(session -> session.createQuery(
                        "from AppUser u where u.role = :role and u.active = true "
                                + "order by u.lastName, u.firstName", AppUser.class)
                .setParameter("role", role)
                .getResultList());
    }

    /** Résumés (id, nom, e-mail) des utilisateurs actifs d'un rôle : alimente les JSP contrats. */
    public List<UserSummary> findSummariesByRole(UserRole role) {
        return read(session -> session.createQuery(
                        SUMMARY_SELECT + "where u.role = :role and u.active = true "
                                + "order by u.lastName, u.firstName", UserSummary.class)
                .setParameter("role", role)
                .getResultList());
    }

    public Optional<UserSummary> findSummaryById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return read(session -> session.createQuery(
                        SUMMARY_SELECT + "where u.id = :id", UserSummary.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .uniqueResultOptional());
    }

    /** Pratique dans les JSP : ${clients[contrat.clientId].fullName}. */
    public Map<Long, UserSummary> mapSummariesByRole(UserRole role) {
        Map<Long, UserSummary> map = new LinkedHashMap<>();
        for (UserSummary summary : findSummariesByRole(role)) {
            map.put(summary.getId(), summary);
        }
        return map;
    }
}
