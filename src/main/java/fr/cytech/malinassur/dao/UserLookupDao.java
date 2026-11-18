package fr.cytech.malinassur.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.cytech.malinassur.dto.UserSummary;
import fr.cytech.malinassur.model.AppUser;

/**
 * Lecture SEULE des utilisateurs, pour afficher le nom des clients et agents
 * dans les pages contrats. Requêtes SQL natives sur app_user : fonctionne même
 * tant que la classe AppUser du module 1 ne contient que l'id.
 */
public class UserLookupDao extends BaseDao<AppUser> {

    private static final String SELECT =
            "select id, first_name, last_name, email from app_user ";

    public UserLookupDao() {
        super(AppUser.class);
    }

    /** Tous les utilisateurs actifs d'un rôle : "CLIENT", "AGENT" ou "ADMIN". */
    public List<UserSummary> findByRole(String role) {
        return read(s -> s.createNativeQuery(
                SELECT + "where role = :role and is_active = true order by last_name, first_name", Object[].class)
                .setParameter("role", role)
                .getResultList()
                .stream().map(UserLookupDao::toSummary).toList());
    }

    public Optional<UserSummary> findSummaryById(Long id) {
        if (id == null) return Optional.empty();
        return read(s -> s.createNativeQuery(SELECT + "where id = :id", Object[].class)
                .setParameter("id", id)
                .getResultList()
                .stream().map(UserLookupDao::toSummary).findFirst());
    }

    /** Pratique dans les JSP : ${clients[contrat.clientId].fullName} */
    public Map<Long, UserSummary> mapByRole(String role) {
        Map<Long, UserSummary> map = new LinkedHashMap<>();
        for (UserSummary u : findByRole(role)) map.put(u.getId(), u);
        return map;
    }

    private static UserSummary toSummary(Object[] row) {
        return new UserSummary(((Number) row[0]).longValue(),
                (String) row[1], (String) row[2], (String) row[3]);
    }
}
