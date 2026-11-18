package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.util.List;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;
import fr.cytech.malinassur.service.UserAdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    private final UserAdminService userAdminService = new UserAdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AppUser currentAdmin = getAuthenticatedAdmin(req, resp);
        if (currentAdmin == null) return;

        String action = req.getParameter("action");

        try {
            if ("new".equals(action)) {
                req.setAttribute("roles", UserRole.values());
                req.setAttribute("defaultRole", req.getParameter("role") != null ? req.getParameter("role") : "CLIENT");
                req.getRequestDispatcher("/WEB-INF/views/admin/user-form.jsp").forward(req, resp);
                return;
            }

            if ("edit".equals(action)) {
                Long id = parseLong(req.getParameter("id"));
                AppUser targetUser = userAdminService.getById(id);
                req.setAttribute("targetUser", targetUser);
                req.setAttribute("roles", UserRole.values());
                req.getRequestDispatcher("/WEB-INF/views/admin/user-form.jsp").forward(req, resp);
                return;
            }

            // Par défaut : affichage de la liste (filtrable par rôle et mot-clé)
            String keyword = req.getParameter("q");
            String roleFilter = req.getParameter("role");
            List<AppUser> users = userAdminService.listUsers(keyword, roleFilter);

            req.setAttribute("users", users);
            req.setAttribute("keyword", keyword);
            req.setAttribute("roleFilter", roleFilter);
            req.setAttribute("roles", UserRole.values());
            req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);

        } catch (BusinessException e) {
            req.getSession().setAttribute("flashError", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AppUser currentAdmin = getAuthenticatedAdmin(req, resp);
        if (currentAdmin == null) return;

        String action = req.getParameter("action");

        try {
            if ("create".equals(action)) {
                String firstName = req.getParameter("firstName");
                String lastName = req.getParameter("lastName");
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                String role = req.getParameter("role");
                boolean active = req.getParameter("active") != null;

                userAdminService.createUser(firstName, lastName, email, password, role, active);
                req.getSession().setAttribute("flashSuccess", "Utilisateur créé avec succès.");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }

            if ("update".equals(action)) {
                Long id = parseLong(req.getParameter("id"));
                String firstName = req.getParameter("firstName");
                String lastName = req.getParameter("lastName");
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                String role = req.getParameter("role");
                boolean active = req.getParameter("active") != null;

                userAdminService.updateUser(id, firstName, lastName, email, password, role, active, currentAdmin.getId());
                req.getSession().setAttribute("flashSuccess", "Utilisateur mis à jour avec succès.");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }

            if ("toggleStatus".equals(action)) {
                Long id = parseLong(req.getParameter("id"));
                userAdminService.toggleActiveStatus(id, currentAdmin.getId());
                req.getSession().setAttribute("flashSuccess", "Statut du compte mis à jour.");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }

            if ("delete".equals(action)) {
                Long id = parseLong(req.getParameter("id"));
                userAdminService.deleteUser(id, currentAdmin.getId());
                req.getSession().setAttribute("flashSuccess", "Utilisateur supprimé.");
                resp.sendRedirect(req.getContextPath() + "/admin/users");
                return;
            }

            resp.sendRedirect(req.getContextPath() + "/admin/users");

        } catch (BusinessException e) {
            if ("create".equals(action) || "update".equals(action)) {
                req.setAttribute("error", e.getMessage());
                req.setAttribute("roles", UserRole.values());
                if ("update".equals(action)) {
                    Long id = parseLong(req.getParameter("id"));
                    if (id != null) {
                        req.setAttribute("targetUser", userAdminService.getById(id));
                    }
                }
                req.getRequestDispatcher("/WEB-INF/views/admin/user-form.jsp").forward(req, resp);
            } else {
                req.getSession().setAttribute("flashError", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/admin/users");
            }
        }
    }

    private AppUser getAuthenticatedAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        AppUser user = null;
        if (session != null) {
            Object u = session.getAttribute("user");
            if (u == null) u = session.getAttribute("currentUser");
            if (u instanceof AppUser) user = (AppUser) u;
        }
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        if (user.getRole() != UserRole.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès réservé aux administrateurs.");
            return null;
        }
        return user;
    }

    private Long parseLong(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}