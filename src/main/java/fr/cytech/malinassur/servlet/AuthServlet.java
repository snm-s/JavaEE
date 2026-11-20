package fr.cytech.malinassur.servlet;

import java.io.IOException;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;
import fr.cytech.malinassur.service.AuthService;
import fr.cytech.malinassur.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Authentification : connexion, inscription et déconnexion.
 *
 * <pre>
 *   GET  /login     → formulaire de connexion
 *   POST /login     → vérification BCrypt + création de session
 *   GET  /register  → formulaire d'inscription (rôle CLIENT)
 *   POST /register  → création du compte
 *   GET|POST /logout → invalidation de la session
 * </pre>
 *
 * <p>La session est entièrement régénérée après une connexion réussie
 * (protection contre la fixation de session).</p>
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    private static final String LOGIN_VIEW = "common/login.jsp";
    private static final String REGISTER_VIEW = "common/register.jsp";

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            logout(req, resp);
            return;
        }

        // Un utilisateur déjà connecté n'a rien à faire sur ces formulaires.
        AppUser current = SessionUser.get(req);
        if (current != null) {
            resp.sendRedirect(req.getContextPath() + current.getRole().getHomePath());
            return;
        }

        noStore(resp);

        if ("/register".equals(path)) {
            forward(req, resp, REGISTER_VIEW);
            return;
        }

        if ("1".equals(req.getParameter("logout"))) {
            req.setAttribute("success", "Vous êtes déconnecté.");
        } else if ("1".equals(req.getParameter("registered"))) {
            req.setAttribute("success", "Votre compte a été créé. Vous pouvez maintenant vous connecter.");
        }
        forward(req, resp, LOGIN_VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();

        switch (path) {
            case "/login" -> login(req, resp);
            case "/register" -> register(req, resp);
            case "/logout" -> logout(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    // ------------------------------------------------------------------
    // Connexion
    // ------------------------------------------------------------------

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String rawEmail = req.getParameter("email");
        String password = req.getParameter("password");
        String emailForView = AuthService.normalizeEmail(rawEmail);

        try {
            AppUser user = authService.authenticate(rawEmail, password);
            openSession(req, user);
            String target = safeRedirect(req.getParameter("redirect"), user.getRole());
            resp.sendRedirect(req.getContextPath() + target);

        } catch (BusinessException e) {
            noStore(resp);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("email", emailForView);
            forward(req, resp, LOGIN_VIEW);
        }
    }

    // ------------------------------------------------------------------
    // Inscription
    // ------------------------------------------------------------------

    private void register(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String firstName = req.getParameter("firstName");
        String lastName = req.getParameter("lastName");
        String rawEmail = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmation = req.getParameter("confirmPassword");

        try {
            authService.register(firstName, lastName, rawEmail, password, confirmation);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");

        } catch (BusinessException e) {
            noStore(resp);
            req.setAttribute("error", e.getMessage());
            req.setAttribute("firstName", firstName);
            req.setAttribute("lastName", lastName);
            req.setAttribute("email", AuthService.normalizeEmail(rawEmail));
            forward(req, resp, REGISTER_VIEW);
        }
    }

    // ------------------------------------------------------------------
    // Déconnexion
    // ------------------------------------------------------------------

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login?logout=1");
    }

    // ------------------------------------------------------------------
    // Session et redirections
    // ------------------------------------------------------------------

    /**
     * Invalide l'ancienne session puis en crée une nouvelle : l'identifiant de session
     * change après l'authentification (protection contre la fixation de session).
     */
    private void openSession(HttpServletRequest req, AppUser user) {
        HttpSession oldSession = req.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute(SessionUser.USER_ATTRIBUTE, user);
        session.setAttribute(SessionUser.ID_ATTRIBUTE, user.getId());
        session.setAttribute(SessionUser.ROLE_ATTRIBUTE, user.getRole());
        session.setAttribute(SessionUser.ROLE_LABEL_ATTRIBUTE, user.getRole().getLabel());
        session.setAttribute(SessionUser.NAME_ATTRIBUTE, user.getFullName());
    }

    /**
     * N'accepte qu'un chemin interne compatible avec le rôle de l'utilisateur.
     * Toute valeur douteuse est remplacée par la page d'accueil du rôle.
     */
    private static String safeRedirect(String target, UserRole role) {
        if (target == null) {
            return role.getHomePath();
        }
        String clean = target.trim();
        boolean safe = clean.startsWith("/")
                && !clean.startsWith("//")
                && !clean.contains("\\")
                && !clean.contains("..")
                && !clean.contains("\r")
                && !clean.contains("\n")
                && clean.indexOf(':') < 0;
        return safe && role.canAccess(clean) ? clean : role.getHomePath();
    }

    private static void noStore(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }
}
