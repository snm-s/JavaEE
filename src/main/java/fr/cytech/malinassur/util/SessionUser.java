package fr.cytech.malinassur.util;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Accès centralisé à l'utilisateur stocké en session.
 *
 * <p>Une seule convention pour toute l'application : la session contient
 * l'attribut {@code user} (entité {@link AppUser} détachée) et {@code userId}.
 * Aucun identifiant de test n'est utilisé en repli.</p>
 */
public final class SessionUser {

    public static final String USER_ATTRIBUTE = "user";
    public static final String ID_ATTRIBUTE = "userId";
    public static final String ROLE_ATTRIBUTE = "role";
    public static final String ROLE_LABEL_ATTRIBUTE = "roleLabel";
    public static final String NAME_ATTRIBUTE = "userName";

    private SessionUser() {
    }

    /** Utilisateur connecté, ou {@code null} si la session est absente ou non authentifiée. */
    public static AppUser get(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(USER_ATTRIBUTE);
        return value instanceof AppUser user ? user : null;
    }

    /** Utilisateur connecté ; lève une erreur métier si la session a expiré. */
    public static AppUser require(HttpServletRequest request) {
        AppUser user = get(request);
        if (user == null) {
            throw new BusinessException("Votre session a expiré. Veuillez vous reconnecter.");
        }
        return user;
    }

    public static Long requireId(HttpServletRequest request) {
        return require(request).getId();
    }
}
