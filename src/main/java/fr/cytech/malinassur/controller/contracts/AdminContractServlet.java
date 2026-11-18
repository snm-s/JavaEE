package fr.cytech.malinassur.controller.contracts;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.dao.UserLookupDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.service.CatalogService;
import fr.cytech.malinassur.service.ContractService;

/**
 * Espace ADMIN – consultation de l'ensemble des contrats (lecture seule).
 *   GET /admin/contracts          → liste + recherche
 *   GET /admin/contracts?id=12    → fiche du contrat
 */
@WebServlet("/admin/contracts")
public class AdminContractServlet extends HttpServlet {

    private final ContractService contracts = new ContractService();
    private final CatalogService catalog = new CatalogService();
    private final UserLookupDao users = new UserLookupDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("role", "admin");
        req.setAttribute("readOnly", true);
        try {
            Long id = WebHelper.optionalLong(req, "id");
            if (id != null) {
                ContractPages.prepareDetails(req, id, contracts, users);
                WebHelper.forward(req, resp, "agent/contract-details.jsp");
            } else {
                ContractPages.prepareList(req, contracts, catalog, users);
                WebHelper.forward(req, resp, "admin/contracts.jsp");
            }
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
            WebHelper.redirect(req, resp, "/admin/contracts");
        }
    }
}
