package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

public class AppUserDao extends BaseDao<AppUser> {

    public AppUserDao() {
        super(AppUser.class);
    }

    public Optional<AppUser> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return read(session -> session.createQuery(
                        "SELECT u FROM AppUser u WHERE LOWER(u.email) = LOWER(:email)", AppUser.class)
                .setParameter("email", email.trim())
                .uniqueResultOptional());
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public boolean existsByEmailAndIdNot(String email, Long excludedId) {
        if (email == null) return false;
        return read(session -> session.createQuery(
                        "SELECT COUNT(u) FROM AppUser u WHERE LOWER(u.email) = LOWER(:email) AND u.id <> :id", Long.class)
                .setParameter("email", email.trim())
                .setParameter("id", excludedId)
                .uniqueResult()) > 0;
    }

    public List<AppUser> findByRoleOrderByCreatedAtDesc(UserRole role) {
        return read(session -> session.createQuery(
                        "SELECT u FROM AppUser u WHERE u.role = :role ORDER BY u.createdAt DESC", AppUser.class)
                .setParameter("role", role)
                .getResultList());
    }

    public List<AppUser> findAllOrderByCreatedAtDesc() {
        return read(session -> session.createQuery(
                        "SELECT u FROM AppUser u ORDER BY u.createdAt DESC", AppUser.class)
                .getResultList());
    }

    public List<AppUser> searchUsers(String keyword, UserRole role) {
        String pattern = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return read(session -> {
            String hql = "SELECT u FROM AppUser u WHERE " +
                    "(LOWER(u.firstName) LIKE :kw OR LOWER(u.lastName) LIKE :kw OR LOWER(u.email) LIKE :kw) " +
                    (role != null ? "AND u.role = :role " : "") +
                    "ORDER BY u.createdAt DESC";
            var query = session.createQuery(hql, AppUser.class).setParameter("kw", pattern);
            if (role != null) {
                query.setParameter("role", role);
            }
            return query.getResultList();
        });
    }
}