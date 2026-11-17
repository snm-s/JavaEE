package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.Claim;
import fr.cytech.malinassur.model.ClaimStatus;
import fr.cytech.malinassur.service.ClaimService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/agent/claims")
public class AgentClaimServlet extends HttpServlet {

    private final ClaimService claimService = new ClaimService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        String statusParam = req.getParameter("status");

        try {
            if (idParam != null && !idParam.isBlank()) {
                Long claimId = Long.parseLong(idParam);
                Claim claim = claimService.getWithDocuments(claimId);
                req.setAttribute("claim", claim);
                req.getRequestDispatcher("/WEB-INF/views/agent/claim-details.jsp").forward(req, resp);
                return;
            }

            List<Claim> claims;
            if (statusParam != null && !statusParam.isBlank()) {
                claims = claimService.findByStatus(ClaimStatus.valueOf(statusParam));
                req.setAttribute("selectedStatus", statusParam);
            } else {
                claims = claimService.findAll();
            }

            req.setAttribute("claims", claims);
            req.setAttribute("statuses", ClaimStatus.values());
            req.getRequestDispatcher("/WEB-INF/views/agent/claims.jsp").forward(req, resp);

        } catch (BusinessException | IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("claims", claimService.findAll());
            req.getRequestDispatcher("/WEB-INF/views/agent/claims.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Long agentId = resolveAgentId(req);
        String claimIdStr = req.getParameter("claimId");
        String action = req.getParameter("action");

        try {
            Long claimId = Long.parseLong(claimIdStr);
            String comment = req.getParameter("agentComment");

            switch (action) {
                case "review" -> {
                    claimService.takeUnderReview(claimId, agentId);
                    req.getSession().setAttribute("success", "Dossier pris en charge (EN_ANALYSE).");
                }
                case "accept" -> {
                    BigDecimal indemnity = new BigDecimal(req.getParameter("indemnityAmount").replace(",", "."));
                    claimService.acceptClaim(claimId, agentId, indemnity, comment);
                    req.getSession().setAttribute("success", "Sinistre accepté et montant d'indemnisation validé.");
                }
                case "reject" -> {
                    claimService.rejectClaim(claimId, agentId, comment);
                    req.getSession().setAttribute("success", "Sinistre refusé.");
                }
                case "indemnify" -> {
                    claimService.markAsIndemnified(claimId, agentId);
                    req.getSession().setAttribute("success", "Indemnisation versée (dossier clôturé).");
                }
                default -> throw new BusinessException("Action inconnue.");
            }

            resp.sendRedirect(req.getContextPath() + "/agent/claims?id=" + claimId);

        } catch (Exception e) {
            req.getSession().setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/agent/claims" + (claimIdStr != null ? "?id=" + claimIdStr : ""));
        }
    }

    private Long resolveAgentId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj instanceof AppUser u) return u.getId();
            Object idObj = session.getAttribute("userId");
            if (idObj instanceof Long id) return id;
        }
        return 2L; // Marc Martin (agent de test)
    }
}