package fr.cytech.malinassur.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import fr.cytech.malinassur.dao.AppUserDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

public class AuthService {

    private final AppUserDao userDao = new AppUserDao();

    /** Inscrit un nouvel utilisateur avec le rôle CLIENT. */
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

    /** Authentifie un utilisateur via son e-mail et son mot de passe. */
    public AppUser authenticate(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new BusinessException("Veuillez renseigner votre e-mail et votre mot de passe.");
        }

        AppUser user = userDao.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Identifiants incorrects."));

        if (!user.isActive()) {
            throw new BusinessException("Ce compte est désactivé. Contactez un administrateur.");
        }

        String hashedInput = hashPassword(rawPassword);
        boolean matchesHash = user.getPasswordHash().equalsIgnoreCase(hashedInput);
        boolean matchesPlainDemo = user.getPasswordHash().equals(rawPassword);

        if (!matchesHash && !matchesPlainDemo) {
            throw new BusinessException("Identifiants incorrects.");
        }

        // Migration silencieuse si le mot de passe en base était en clair (données SQL de démo)
        if (matchesPlainDemo && !matchesHash) {
            user.setPasswordHash(hashedInput);
            user = userDao.update(user);
        }

        return user;
    }

    public static String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme SHA-256 indisponible", e);
        }
    }
}