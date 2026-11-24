package fr.cytech.malinassur.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import fr.cytech.malinassur.dao.AppUserDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

public class AuthService {

    private final AppUserDao userDao = new AppUserDao();

    /** Inscrit un nouvel utilisateur avec le rôle CLIENT (hash BCrypt). */
    public AppUser registerClient(String firstName, String lastName, String email, String rawPassword, String confirmPassword) {
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessException("Le prénom est obligatoire.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessException("Le nom est obligatoire.");
        }
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessException("Veuillez saisir une adresse e-mail valide.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new BusinessException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        if (!rawPassword.equals(confirmPassword)) {
            throw new BusinessException("Les deux mots de passe ne correspondent pas.");
        }
        if (userDao.existsByEmail(email)) {
            throw new BusinessException("Un compte existe déjà avec cette adresse e-mail.");
        }

        AppUser user = new AppUser(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                hashPassword(rawPassword),
                UserRole.CLIENT
        );
        return userDao.save(user);
    }

    /** Authentifie un utilisateur via BCrypt (avec repli rétrocompatible). */
    public AppUser authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("Veuillez renseigner votre e-mail et votre mot de passe.");
        }

        AppUser user = userDao.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Identifiants incorrects."));

        if (!user.isActive()) {
            throw new BusinessException("Ce compte est désactivé. Contactez un administrateur.");
        }

        String storedHash = user.getPasswordHash();
        boolean authenticated = false;

        if (storedHash != null && storedHash.startsWith("$2")) {
            // Vérification BCrypt standard ($2a$, $2b$, $2y$)
            authenticated = BCrypt.checkpw(rawPassword, storedHash);
        } else if (storedHash != null && (storedHash.equals(rawPassword) || storedHash.equalsIgnoreCase(sha256(rawPassword)))) {
            // Rétrocompatibilité si un ancien compte était en clair ou SHA-256
            authenticated = true;
            user.setPasswordHash(hashPassword(rawPassword));
            user = userDao.update(user);
        }

        if (!authenticated) {
            throw new BusinessException("Identifiants incorrects.");
        }

        return user;
    }

    public static String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    private static String sha256(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible", e);
        }
    }

    // --- Méthodes d'administration si utilisées par AdminUserServlet ---
    public List<AppUser> findAllUsers() {
        return userDao.findAll();
    }

    public void updateUserRole(Long targetUserId, UserRole newRole, Long currentAdminId) {
        if (targetUserId.equals(currentAdminId)) {
            throw new BusinessException("Vous ne pouvez pas modifier votre propre rôle.");
        }
        AppUser user = userDao.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        user.setRole(newRole);
        userDao.update(user);
    }

    public void toggleUserStatus(Long targetUserId, Long currentAdminId) {
        if (targetUserId.equals(currentAdminId)) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        AppUser user = userDao.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
        user.setActive(!user.isActive());
        userDao.update(user);
    }
}