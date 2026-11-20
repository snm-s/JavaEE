package fr.cytech.malinassur.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import org.hibernate.exception.ConstraintViolationException;
import org.mindrot.jbcrypt.BCrypt;

import fr.cytech.malinassur.dao.UserDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

/**
 * Authentification et inscription.
 *
 * <p>Règles de sécurité appliquées :</p>
 * <ul>
 *   <li>mots de passe hachés avec BCrypt (jamais stockés en clair) ;</li>
 *   <li>messages d'erreur génériques pour ne pas révéler l'existence d'un compte ;</li>
 *   <li>validation systématique des saisies côté serveur ;</li>
 *   <li>requêtes HQL paramétrées via {@link UserDao}.</li>
 * </ul>
 */
public class AuthService {

    private static final String INVALID_CREDENTIALS = "E-mail ou mot de passe incorrect.";
    private static final int BCRYPT_COST = 10;
    private static final int MAX_EMAIL_LENGTH = 255;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_BYTES = 72;

    /**
     * Hash factice utilisé pour effectuer un calcul BCrypt même lorsqu'aucun compte
     * ne correspond, afin de limiter les attaques temporelles.
     */
    private static final String DUMMY_HASH =
            "$2a$10$95OH7KWpN6KlNOUa2ETfWuBkpwLHcC71ZR1Ng3N29dXjIeYPP3KgC";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[\\p{L}][\\p{L} '\\-]{0,99}$");

    private final UserDao userDao = new UserDao();

    /**
     * Vérifie les identifiants et renvoie l'utilisateur authentifié.
     *
     * @throws BusinessException message volontairement générique si l'authentification échoue
     */
    public AppUser authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null || password == null || password.isEmpty()) {
            BCrypt.checkpw(password == null ? "" : password, DUMMY_HASH);
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        AppUser user = userDao.findByEmail(normalizedEmail).orElse(null);
        boolean passwordMatches = user != null && matches(password, user.getPasswordHash());

        if (user == null) {
            // Calcul équivalent pour ne pas distinguer « compte inconnu » et « mot de passe faux ».
            BCrypt.checkpw(password, DUMMY_HASH);
        }
        if (!passwordMatches || !user.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }
        return user;
    }

    /**
     * Crée un compte client. Le rôle n'est jamais choisi par le formulaire public :
     * il est fixé à {@link UserRole#CLIENT} pour empêcher toute élévation de privilèges.
     */
    public AppUser register(String firstName, String lastName, String email,
                            String password, String confirmation) {
        String cleanFirstName = requireName(firstName, "Le prénom");
        String cleanLastName = requireName(lastName, "Le nom");
        String cleanEmail = requireEmail(email);
        requirePassword(password);

        if (confirmation == null || !password.equals(confirmation)) {
            throw new BusinessException("Les deux mots de passe ne correspondent pas.");
        }
        if (userDao.existsByEmail(cleanEmail)) {
            throw new BusinessException("Un compte existe déjà avec cette adresse e-mail.");
        }

        AppUser user = new AppUser(
                cleanFirstName, cleanLastName, cleanEmail,
                hash(password), UserRole.CLIENT);

        try {
            return userDao.save(user);
        } catch (RuntimeException e) {
            // Course entre deux inscriptions simultanées : la contrainte UNIQUE tranche.
            if (isUniqueViolation(e)) {
                throw new BusinessException("Un compte existe déjà avec cette adresse e-mail.");
            }
            throw new BusinessException("Impossible de créer le compte. Veuillez réessayer.");
        }
    }

    /** Hache un mot de passe avec un sel aléatoire BCrypt. */
    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    /** Vérifie un mot de passe ; renvoie false si le hash stocké est invalide. */
    public static boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, passwordHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Normalise un e-mail (espaces supprimés, minuscules) ; null si vide. */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------
    // Validation des saisies
    // ------------------------------------------------------------------

    private static String requireName(String value, String label) {
        String clean = value == null ? "" : value.trim();
        if (clean.isEmpty()) {
            throw new BusinessException(label + " est obligatoire.");
        }
        if (clean.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(label + " ne doit pas dépasser " + MAX_NAME_LENGTH + " caractères.");
        }
        if (!NAME_PATTERN.matcher(clean).matches()) {
            throw new BusinessException(label + " contient des caractères non autorisés.");
        }
        return clean;
    }

    private static String requireEmail(String value) {
        String clean = normalizeEmail(value);
        if (clean == null) {
            throw new BusinessException("L'adresse e-mail est obligatoire.");
        }
        if (clean.length() > MAX_EMAIL_LENGTH) {
            throw new BusinessException("L'adresse e-mail est trop longue.");
        }
        if (!EMAIL_PATTERN.matcher(clean).matches()) {
            throw new BusinessException("L'adresse e-mail n'est pas valide.");
        }
        return clean;
    }

    private static void requirePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException("Le mot de passe est obligatoire.");
        }
        if (password.contains("\n") || password.contains("\r")) {
            throw new BusinessException("Le mot de passe ne doit pas contenir de retour à la ligne.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException("Le mot de passe doit contenir au moins " + MIN_PASSWORD_LENGTH + " caractères.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BusinessException("Le mot de passe est trop long (72 octets maximum).");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException("Le mot de passe doit contenir au moins une lettre et un chiffre.");
        }
    }

    private static boolean isUniqueViolation(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase(Locale.ROOT);
                if (lower.contains("duplicate") || lower.contains("unique") || lower.contains("uk_")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
