package fr.cytech.malinassur.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;
import fr.cytech.malinassur.util.SessionUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtre d'authentification et de contrôle d'accès.
 *
 * <ul>
 *   <li>les ressources publiques (accueil, connexion, inscription, CSS, JS, images)
 *       restent accessibles sans session ;</li>
 *   <li>toute autre URL exige une session authentifiée, sinon redirection vers
 *       {@code /login} avec mémorisation de la cible ;</li>
 *   <li>les espaces {@code /client/*}, {@code /agent/*} et {@code /admin/*} sont
 *       réservés au rôle correspondant (403 sinon) ;</li>
 *   <li>les fichiers déposés ne sont jamais servis directement : ils passent
 *       obligatoirement par {@code /documents/download} qui contrôle la propriété.</li>
 * </ul>
 */
@WebFilter(urlPatterns = "/*")
public class AuthFilter implements Filter {

    private static final String ACCESS_DENIED_VIEW = "/WEB-INF/views/common/access-denied.jsp";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setHeader("X-Content-Type-Options", "nosniff");

        String path = req.getRequestURI().substring(req.getContextPath().length());
        if (path.isEmpty()) {
            path = "/";
        }

        // Les pièces jointes ne doivent être accessibles que via la servlet de téléchargement.
        if (path.equals("/uploads") || path.startsWith("/uploads/")) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        AppUser user = SessionUser.get(req);
        if (user == null) {
            String target = path;
            String query = req.getQueryString();
            if (query != null && !query.isBlank()) {
                target += "?" + query;
            }
            String redirect = URLEncoder.encode(target, StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/login?redirect=" + redirect);
            return;
        }

        UserRole requiredRole = requiredRole(path);
        if (requiredRole != null && user.getRole() != requiredRole) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("homePath", user.getRole().getHomePath());
            req.getRequestDispatcher(ACCESS_DENIED_VIEW).forward(req, resp);
            return;
        }

        // Les pages privées ne doivent pas rester dans le cache du navigateur.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");

        chain.doFilter(request, response);
    }

    private static boolean isPublic(String path) {
        return path.equals("/")
                || path.equals("/index.jsp")
                || path.equals("/login")
                || path.equals("/register")
                || path.equals("/logout")
                || path.equals("/favicon.ico")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/");
    }

    private static UserRole requiredRole(String path) {
        if (path.equals("/admin") || path.startsWith("/admin/")) {
            return UserRole.ADMIN;
        }
        if (path.equals("/agent") || path.startsWith("/agent/")) {
            return UserRole.AGENT;
        }
        if (path.equals("/client") || path.startsWith("/client/")) {
            return UserRole.CLIENT;
        }
        return null; // authentification seule (ex. /documents/download)
    }
}
