package fr.cytech.malinassur.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.cytech.malinassur.model.SupportingDocument;
import fr.cytech.malinassur.service.ClaimService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/documents/download")
public class DocumentDownloadServlet extends HttpServlet {

    private final ClaimService claimService = new ClaimService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identifiant de document manquant.");
            return;
        }

        try {
            Long docId = Long.parseLong(idParam);
            SupportingDocument doc = claimService.getDocumentById(docId);

            String baseWebapp = getServletContext().getRealPath("/");
            Path filePath = (baseWebapp != null)
                    ? Paths.get(baseWebapp, doc.getStoragePath())
                    : Paths.get(System.getProperty("user.dir"), doc.getStoragePath());

            if (!Files.exists(filePath)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Fichier introuvable sur le serveur.");
                return;
            }

            resp.setContentType(doc.getMimeType() != null ? doc.getMimeType() : "application/octet-stream");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + doc.getFileName() + "\"");
            resp.setContentLengthLong(Files.size(filePath));
            Files.copy(filePath, resp.getOutputStream());

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }
}