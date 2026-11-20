package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.SupportingDocument;
import fr.cytech.malinassur.model.UserRole;
import fr.cytech.malinassur.service.ClaimService;
import fr.cytech.malinassur.util.SessionUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Téléchargement d'une pièce justificative.
 *
 * <p>L'accès est réservé aux utilisateurs authentifiés. Un client ne peut ouvrir
 * que les documents rattachés à ses propres contrats ; les agents et administrateurs
 * peuvent consulter tous les dossiers. Les fichiers déposés ne sont jamais exposés
 * directement par le serveur (le filtre bloque {@code /uploads/*}).</p>
 */
@WebServlet("/documents/download")
public class DocumentDownloadServlet extends HttpServlet {

    private final ClaimService claimService = new ClaimService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AppUser user = SessionUser.get(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentification requise.");
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identifiant de document manquant.");
            return;
        }

        try {
            Long docId = Long.parseLong(idParam);
            SupportingDocument doc = claimService.getDocumentWithClaim(docId);

            if (user.getRole() == UserRole.CLIENT) {
                Long ownerId = doc.getClaim().getContract().getClientId();
                if (ownerId == null || !ownerId.equals(user.getId())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès refusé à ce document.");
                    return;
                }
            }

            String baseWebapp = getServletContext().getRealPath("/");
            Path base = (baseWebapp != null
                    ? Paths.get(baseWebapp)
                    : Paths.get(System.getProperty("user.dir")))
                    .toAbsolutePath().normalize();
            Path filePath = base.resolve(doc.getStoragePath()).normalize();

            // Protection contre les chemins sortant du répertoire de l'application.
            if (!filePath.startsWith(base) || !Files.isRegularFile(filePath)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier introuvable sur le serveur.");
                return;
            }

            String fileName = doc.getFileName() == null ? "document" : doc.getFileName();
            String safeName = fileName.replaceAll("[\\r\\n\"\\\\]", "_");

            resp.setContentType(doc.getMimeType() != null ? doc.getMimeType() : "application/octet-stream");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + safeName + "\"");
            resp.setContentLengthLong(Files.size(filePath));
            Files.copy(filePath, resp.getOutputStream());

        } catch (NumberFormatException | BusinessException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Document introuvable.");
        } catch (IOException e) {
            getServletContext().log("Erreur lors du téléchargement d'un document", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur interne.");
        }
    }
}
