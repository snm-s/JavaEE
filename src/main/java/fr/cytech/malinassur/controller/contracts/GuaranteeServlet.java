package fr.cytech.malinassur.controller.contracts;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Coverage;
import fr.cytech.malinassur.service.CatalogService;

/**
 * Gestion des garanties (consigne : « l'agent pourra gérer les garanties »).
 * La même servlet sert l'espace agent ET l'espace admin.
 *
 *   GET  /agent/guarantees            → liste + formulaire d'ajout
 *   GET  /agent/guarantees?edit=2     → formulaire pré-rempli
 *   POST /agent/guarantees/save       → crée ou modifie
 *   POST /agent/guarantees/delete     → supprime (ou désactive si déjà utilisée dans un contrat)
 *   (mêmes URL en /admin/guarantees/...)
 */
@WebServlet({"/agent/guarantees/*", "/admin/guarantees/*"})
public class GuaranteeServlet extends HttpServlet {

    private final CatalogService catalog = new CatalogService();

    /** "agent" ou "admin", selon l'URL appelée. */
    private static String role(HttpServletRequest req) {
        return req.getServletPath().startsWith("/admin") ? "admin" : "agent";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String role = role(req);
        try {
            req.setAttribute("role", role);
            req.setAttribute("coverages", catalog.allCoverages());
            Long editId = WebHelper.optionalLong(req, "edit");
            if (editId != null) req.setAttribute("editCoverage", catalog.getCoverage(editId));
            WebHelper.forward(req, resp, role + "/guarantees.jsp");
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
            WebHelper.redirect(req, resp, "/" + role + "/guarantees");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String role = role(req);
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            switch (path) {
                case "/save" -> {
                    Long id = WebHelper.optionalLong(req, "id");
                    String name = WebHelper.text(req, "name");
                    String description = WebHelper.text(req, "description");
                    String type = WebHelper.text(req, "coverageType");
                    if (id == null) {
                        Coverage c = catalog.createCoverage(WebHelper.text(req, "code"), name, description, type);
                        WebHelper.flashSuccess(req, "Garantie « " + c.getName() + " » créée.");
                    } else {
                        Coverage c = catalog.updateCoverage(id, name, description, type, WebHelper.checkbox(req, "active"));
                        WebHelper.flashSuccess(req, "Garantie « " + c.getName() + " » modifiée.");
                    }
                }
                case "/delete" -> {
                    boolean deleted = catalog.deleteCoverage(WebHelper.requiredLong(req, "id", "La garantie"));
                    WebHelper.flashSuccess(req, deleted
                            ? "Garantie supprimée."
                            : "Cette garantie est utilisée dans des contrats : elle a été désactivée au lieu d'être supprimée.");
                }
                default -> {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
        }
        WebHelper.redirect(req, resp, "/" + role + "/guarantees");
    }
}
