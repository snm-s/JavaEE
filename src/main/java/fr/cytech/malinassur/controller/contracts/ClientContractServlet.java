package fr.cytech.malinassur.controller.contracts;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.model.ContractStatus;
import fr.cytech.malinassur.service.ContractService;

/**
 * Espace CLIENT – consultation de SES contrats et de SES garanties.
 *
 *   GET /client/contracts               → mes contrats
 *   GET /client/contracts/details?id=   → détail d'un de mes contrats
 *   GET /client/guarantees              → mes garanties (par contrat actif)
 */
@WebServlet({"/client/contracts", "/client/contracts/details", "/client/guarantees"})
public class ClientContractServlet extends HttpServlet {

    private final ContractService contracts = new ContractService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("role", "client");
        try {
            Long clientId = WebHelper.currentUserId(req);
            contracts.expireOutdatedContracts();

            switch (req.getServletPath()) {
                case "/client/contracts/details" -> {
                    Contract c = contracts.get(WebHelper.requiredLong(req, "id", "Le contrat"));
                    // Sécurité : un client ne voit QUE ses propres contrats
                    if (!c.getClientId().equals(clientId)) {
                        throw new BusinessException("Ce contrat ne vous appartient pas.");
                    }
                    req.setAttribute("contract", c);
                    WebHelper.forward(req, resp, "client/contract-details.jsp");
                }
                case "/client/guarantees" -> {
                    List<Contract> active = contracts.findByClient(clientId).stream()
                            .filter(c -> c.getStatus() == ContractStatus.ACTIF)
                            .toList();
                    req.setAttribute("contracts", active);
                    WebHelper.forward(req, resp, "client/guarantees.jsp");
                }
                default -> {
                    req.setAttribute("contracts", contracts.findByClient(clientId));
                    WebHelper.forward(req, resp, "client/contracts.jsp");
                }
            }
        } catch (BusinessException e) {
            if ("/client/contracts".equals(req.getServletPath())) {
                // erreur sur la liste elle-même : on affiche le message (pas de redirection en boucle)
                req.setAttribute("error", e.getMessage());
                req.setAttribute("contracts", List.of());
                WebHelper.forward(req, resp, "client/contracts.jsp");
            } else {
                WebHelper.flashError(req, e.getMessage());
                WebHelper.redirect(req, resp, "/client/contracts");
            }
        }
    }
}
