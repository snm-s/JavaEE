package fr.cytech.malinassur.controller.contracts;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.InsuranceType;
import fr.cytech.malinassur.model.InsuranceTypeCoverage;
import fr.cytech.malinassur.service.CatalogService;

/**
 * Espace ADMIN – types d'assurance et leur catalogue de garanties.
 *
 *   GET  /admin/insurance-types              → liste + formulaire d'ajout
 *   GET  /admin/insurance-types?edit=3       → même page, formulaire pré-rempli pour le type 3
 *   POST /admin/insurance-types/save         → crée ou modifie un type
 *   POST /admin/insurance-types/offer        → ajoute / met à jour une garantie dans le catalogue d'un type
 *   POST /admin/insurance-types/withdraw     → retire une garantie du catalogue d'un type
 */
@WebServlet("/admin/insurance-types/*")
public class InsuranceTypeServlet extends HttpServlet {

    private final CatalogService catalog = new CatalogService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            show(req, resp);
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
            WebHelper.redirect(req, resp, "/admin/insurance-types");
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("role", "admin");
        List<InsuranceType> types = catalog.allTypes();
        Map<Long, List<InsuranceTypeCoverage>> offers = new LinkedHashMap<>();
        Map<Long, Set<Long>> offeredIds = new LinkedHashMap<>();
        for (InsuranceType t : types) {
            List<InsuranceTypeCoverage> list = catalog.offeredCoverages(t.getId());
            offers.put(t.getId(), list);
            Set<Long> ids = new HashSet<>();
            for (InsuranceTypeCoverage itc : list) ids.add(itc.getCoverage().getId());
            offeredIds.put(t.getId(), ids);
        }
        req.setAttribute("types", types);
        req.setAttribute("offers", offers);
        req.setAttribute("offeredIds", offeredIds);
        req.setAttribute("coverages", catalog.activeCoverages());

        Long editId = WebHelper.optionalLong(req, "edit");
        if (editId != null) req.setAttribute("editType", catalog.getType(editId));
        WebHelper.forward(req, resp, "admin/insurance-types.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            switch (path) {
                case "/save" -> {
                    Long id = WebHelper.optionalLong(req, "id");
                    String name = WebHelper.text(req, "name");
                    String description = WebHelper.text(req, "description");
                    var basePrice = WebHelper.requiredAmount(req, "basePrice", "Le prix de base");
                    if (id == null) {
                        InsuranceType t = catalog.createType(WebHelper.text(req, "code"), name, description, basePrice);
                        WebHelper.flashSuccess(req, "Type « " + t.getName() + " » créé. Ajoutez-lui maintenant des garanties.");
                    } else {
                        InsuranceType t = catalog.updateType(id, name, description, basePrice,
                                WebHelper.checkbox(req, "active"));
                        WebHelper.flashSuccess(req, "Type « " + t.getName() + " » modifié.");
                    }
                }
                case "/offer" -> {
                    catalog.offerCoverage(
                            WebHelper.requiredLong(req, "typeId", "Le type"),
                            WebHelper.requiredLong(req, "coverageId", "La garantie"),
                            WebHelper.checkbox(req, "isDefault"),
                            WebHelper.requiredAmount(req, "additionalPremium", "Le supplément"));
                    WebHelper.flashSuccess(req, "Catalogue mis à jour.");
                }
                case "/withdraw" -> {
                    catalog.withdrawCoverage(
                            WebHelper.requiredLong(req, "typeId", "Le type"),
                            WebHelper.requiredLong(req, "coverageId", "La garantie"));
                    WebHelper.flashSuccess(req, "Garantie retirée du catalogue (les contrats déjà signés la gardent).");
                }
                default -> {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
        }
        WebHelper.redirect(req, resp, "/admin/insurance-types");
    }
}
