package fr.cytech.malinassur.filter;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.UserRole;

@WebFilter({"/client/*", "/agent/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        AppUser user = (session != null && session.getAttribute("user") instanceof AppUser u) ? u : null;

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Empêcher un CLIENT d'accéder aux espaces /agent/* et /admin/*
        if ((path.startsWith("/agent/") || path.startsWith("/admin/")) && user.getRole() == UserRole.CLIENT) {
            resp.sendRedirect(req.getContextPath() + "/client/dashboard");
            return;
        }

        // Empêcher un AGENT d'accéder à l'espace /admin/*
        if (path.startsWith("/admin/") && user.getRole() != UserRole.ADMIN) {
            resp.sendRedirect(req.getContextPath() + "/agent/claims");
            return;
        }

        chain.doFilter(request, response);
    }
}