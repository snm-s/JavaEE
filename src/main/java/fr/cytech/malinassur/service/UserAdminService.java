package fr.cytech.malinassur.service;

import java.util.List;
import fr.cytech.malinassur.dao.AppUserDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

public class UserAdminService {

    private final AppUserDao userDao = new AppUserDao();

    public List<AppUser> listUsers(String keyword, String roleFilter) {
        UserRole role = parseRole(roleFilter);
        if (keyword != null && !keyword.isBlank()) {
            return userDao.searchUsers(keyword, role);
        }
        if (role != null) {
            return userDao.findByRoleOrderByCreatedAtDesc(role);
        }
        return userDao.findAllOrderByCreatedAtDesc();
    }

    public AppUser getById(Long id) {
        if (id == null) {
            throw new BusinessException("Identifiant utilisateur manquant.");
        }
        return userDao.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));
    }

    public AppUser createUser(String firstName, String lastName, String email, String rawPassword, String roleStr, boolean active) {
        validateCommonFields(firstName, lastName, email);

        if (rawPassword == null || rawPassword.length() < 6) {
            throw new BusinessException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        if (userDao.existsByEmail(email)) {
            throw new BusinessException("Un compte utilise déjà cette adresse e-mail.");
        }

        UserRole role = parseRoleOrDefault(roleStr, UserRole.CLIENT);
        AppUser user = new AppUser(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                AuthService.hashPassword(rawPassword),
                role
        );
        user.setActive(active);
        return userDao.save(user);
    }

    public AppUser updateUser(Long id, String firstName, String lastName, String email, String newPassword, String roleStr, boolean active, Long currentAdminId) {
        AppUser existing = getById(id);
        validateCommonFields(firstName, lastName, email);

        if (userDao.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException("Cette adresse e-mail est déjà attribuée à un autre compte.");
        }

        UserRole targetRole = parseRoleOrDefault(roleStr, existing.getRole());

        // Sécurité : empêcher un admin de se désactiver ou de se retirer son propre rôle ADMIN
        if (existing.getId().equals(currentAdminId)) {
            if (!active) {
                throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte administrateur.");
            }
            if (targetRole != UserRole.ADMIN) {
                throw new BusinessException("Vous ne pouvez pas rétrograder votre propre rôle administrateur.");
            }
        }

        existing.setFirstName(firstName.trim());
        existing.setLastName(lastName.trim());
        existing.setEmail(email.trim().toLowerCase());
        existing.setRole(targetRole);
        existing.setActive(active);

        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 6) {
                throw new BusinessException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
            }
            existing.setPasswordHash(AuthService.hashPassword(newPassword));
        }

        return userDao.update(existing);
    }

    public void toggleActiveStatus(Long id, Long currentAdminId) {
        AppUser user = getById(id);
        if (user.getId().equals(currentAdminId)) {
            throw new BusinessException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        user.setActive(!user.isActive());
        userDao.update(user);
    }

    public void deleteUser(Long id, Long currentAdminId) {
        AppUser user = getById(id);
        if (user.getId().equals(currentAdminId)) {
            throw new BusinessException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        try {
            userDao.deleteById(id);
        } catch (RuntimeException e) {
            throw new BusinessException("Impossible de supprimer ce client car il possède des contrats, sinistres ou paiements associés. Désactivez plutôt son compte.");
        }
    }

    private void validateCommonFields(String firstName, String lastName, String email) {
        if (firstName == null || firstName.isBlank()) {
            throw new BusinessException("Le prénom est obligatoire.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new BusinessException("Le nom est obligatoire.");
        }
        if (email == null || !email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessException("Veuillez saisir une adresse e-mail valide.");
        }
    }

    private UserRole parseRole(String roleStr) {
        if (roleStr == null || roleStr.isBlank()) return null;
        try {
            return UserRole.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private UserRole parseRoleOrDefault(String roleStr, UserRole defaultRole) {
        UserRole parsed = parseRole(roleStr);
        return parsed != null ? parsed : defaultRole;
    }
}