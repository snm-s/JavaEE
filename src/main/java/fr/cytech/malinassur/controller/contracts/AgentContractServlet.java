package fr.cytech.malinassur.controller.contracts;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.dao.UserDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.service.CatalogService;
import fr.cytech.malinassur.service.ContractService;

/**
 * Espace AGENT – gestion des contrats.
 *
 *   GET  /agent/contracts            → liste + recherche
 *   GET  /agent/contracts/new        → formulaire de création
 *   GET  /agent/contracts/edit?id=   → formulaire de modification
 *   GET  /agent/contracts/view?id=   → fiche du contrat
 *   POST /agent/contracts/save       → enregistre (création si pas d'id, sinon modification)
 *   POST /agent/contracts/status     → op = activate | suspend | terminate | renew | assign
 */
@WebServlet("/agent/contracts/*")
public class AgentContractServlet extends HttpServlet {

    private final ContractService contracts = new ContractService();
    private final CatalogService catalog = new CatalogService();
    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("role", "agent");
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            switch (path) {
                case "/new" -> {
                    ContractPages.prepareForm(req, catalog, users);
                    req.setAttribute("today", LocalDate.now());
                    WebHelper.forward(req, resp, "agent/contract-form.jsp");
                }
                case "/edit" -> {
                    Contract c = contracts.get(WebHelper.requiredLong(req, "id", "Le contrat"));
                    ContractPages.prepareForm(req, catalog, users);
                    req.setAttribute("contract", c);
                    req.setAttribute("selectedIds", ContractPages.selectedCoverageIds(c));
                    WebHelper.forward(req, resp, "agent/contract-form.jsp");
                }
                case "/view" -> {
                    ContractPages.prepareDetails(req, WebHelper.requiredLong(req, "id", "Le contrat"), contracts, users);
                    WebHelper.forward(req, resp, "agent/contract-details.jsp");
                }
                default -> {
                    ContractPages.prepareList(req, contracts, catalog, users);
                    WebHelper.forward(req, resp, "agent/contracts.jsp");
                }
            }
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
            WebHelper.redirect(req, resp, "/agent/contracts");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        switch (path) {
            case "/save" -> save(req, resp);
            case "/status" -> changeStatus(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /** Création ou modification. En cas d'erreur, on réaffiche le formulaire avec le message. */
    private void save(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = null;
        try {
            id = WebHelper.optionalLong(req, "id");
            List<Long> coverageIds = WebHelper.longList(req, "coverageIds");
            Contract saved;
            if (id == null) {
                saved = contracts.create(
                        WebHelper.requiredLong(req, "clientId", "Le client"),
                        WebHelper.requiredLong(req, "typeId", "Le type d'assurance"),
                        coverageIds,
                        WebHelper.requiredDate(req, "startDate", "La date de début"),
                        WebHelper.requiredInt(req, "durationMonths", "La durée"),
                        WebHelper.currentUserId(req));
                WebHelper.flashSuccess(req, "Contrat " + saved.getContractNumber() + " créé (en attente d'activation).");
            } else {
                saved = contracts.update(id,
                        WebHelper.requiredDate(req, "startDate", "La date de début"),
                        WebHelper.requiredDate(req, "endDate", "La date de fin"),
                        coverageIds);
                WebHelper.flashSuccess(req, "Contrat " + saved.getContractNumber() + " modifié.");
            }
            WebHelper.redirect(req, resp, "/agent/contracts/view?id=" + saved.getId());
        } catch (BusinessException e) {
            // on réaffiche le formulaire, sans perdre ce que l'agent a saisi
            req.setAttribute("role", "agent");
            req.setAttribute("error", e.getMessage());
            ContractPages.prepareForm(req, catalog, users);
            req.setAttribute("today", LocalDate.now());
            if (id != null) {
                Contract c = contracts.get(id);
                req.setAttribute("contract", c);
                req.setAttribute("selectedIds", ContractPages.selectedCoverageIds(c));
            }
            WebHelper.forward(req, resp, "agent/contract-form.jsp");
        }
    }

    private void changeStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = null;
        try {
            id = WebHelper.requiredLong(req, "id", "Le contrat");
            String op = WebHelper.text(req, "op");
            Contract c;
            if ("activate".equals(op)) {
                c = contracts.activate(id);
                WebHelper.flashSuccess(req, "Contrat " + c.getContractNumber() + " activé.");
            } else if ("suspend".equals(op)) {
                c = contracts.suspend(id);
                WebHelper.flashSuccess(req, "Contrat " + c.getContractNumber() + " suspendu.");
            } else if ("terminate".equals(op)) {
                c = contracts.terminate(id);
                WebHelper.flashSuccess(req, "Contrat " + c.getContractNumber() + " résilié.");
            } else if ("renew".equals(op)) {
                c = contracts.renew(id);
                WebHelper.flashSuccess(req, "Contrat renouvelé jusqu'au " + c.getEndDate() + ".");
            } else if ("assign".equals(op)) {
                c = contracts.assignAgent(id, WebHelper.requiredLong(req, "agentId", "L'agent"));
                WebHelper.flashSuccess(req, "Agent assigné au contrat " + c.getContractNumber() + ".");
            } else {
                throw new BusinessException("Action inconnue.");
            }
        } catch (BusinessException e) {
            WebHelper.flashError(req, e.getMessage());
        }
        WebHelper.redirect(req, resp, id == null ? "/agent/contracts" : "/agent/contracts/view?id=" + id);
    }
}
