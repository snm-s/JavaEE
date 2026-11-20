package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import fr.cytech.malinassur.dao.ContractDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Claim;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.service.ClaimService;
import fr.cytech.malinassur.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/client/claims")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,      // 1 Mo en mémoire avant écriture disque
        maxFileSize = 10 * 1024 * 1024,       // 10 Mo par fichier
        maxRequestSize = 15 * 1024 * 1024     // 15 Mo par requête
)
public class ClientClaimServlet extends HttpServlet {

    private final ClaimService claimService = new ClaimService();
    private final ContractDao contractDao = new ContractDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long clientId = resolveClientId(req);
        String action = req.getParameter("action");
        String idParam = req.getParameter("id");

        try {
            if ("new".equals(action)) {
                List<Contract> contracts = contractDao.findByClient(clientId);
                req.setAttribute("contracts", contracts);
                req.getRequestDispatcher("/WEB-INF/views/client/claim-form.jsp").forward(req, resp);
                return;
            }

            if (idParam != null && !idParam.isBlank()) {
                Long claimId = Long.parseLong(idParam);
                Claim claim = claimService.getWithDocuments(claimId);
                if (!claim.getContract().getClientId().equals(clientId)) {
                    throw new BusinessException("Accès refusé à ce sinistre.");
                }
                req.setAttribute("claim", claim);
                req.getRequestDispatcher("/WEB-INF/views/client/claim-details.jsp").forward(req, resp);
                return;
            }

            List<Claim> claims = claimService.findByClient(clientId);
            req.setAttribute("claims", claims);
            req.getRequestDispatcher("/WEB-INF/views/client/claims.jsp").forward(req, resp);

        } catch (BusinessException | NumberFormatException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("claims", claimService.findByClient(clientId));
            req.getRequestDispatcher("/WEB-INF/views/client/claims.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Long clientId = resolveClientId(req);

        try {
            Long contractId = Long.parseLong(req.getParameter("contractId"));
            LocalDate incidentDate = LocalDate.parse(req.getParameter("incidentDate"));
            String claimType = req.getParameter("claimType");
            String description = req.getParameter("description");
            BigDecimal estimatedAmount = new BigDecimal(req.getParameter("estimatedAmount").replace(",", "."));

            Claim createdClaim = claimService.declareClaim(
                    clientId, contractId, incidentDate, claimType, description, estimatedAmount
            );

            Part filePart = req.getPart("document");
            if (filePart != null && filePart.getSize() > 0) {
                String uploadDir = getServletContext().getRealPath("/uploads");
                if (uploadDir == null) {
                    uploadDir = System.getProperty("user.dir") + "/uploads";
                }
                claimService.attachDocument(createdClaim.getId(), filePart, uploadDir);
            }

            req.getSession().setAttribute("success", "Sinistre " + createdClaim.getClaimNumber() + " déclaré avec succès.");
            resp.sendRedirect(req.getContextPath() + "/client/claims");

        } catch (Exception e) {
            req.setAttribute("error", "Erreur lors de la déclaration : " + e.getMessage());
            req.setAttribute("contracts", contractDao.findByClient(clientId));
            req.getRequestDispatcher("/WEB-INF/views/client/claim-form.jsp").forward(req, resp);
        }
    }

    private Long resolveClientId(HttpServletRequest req) {
        return SessionUser.requireId(req);
    }
}