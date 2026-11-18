package fr.cytech.malinassur.dao;

import java.util.Optional;
import fr.cytech.malinassur.model.AppUser;

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
}