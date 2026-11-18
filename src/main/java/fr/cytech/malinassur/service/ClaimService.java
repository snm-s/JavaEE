package fr.cytech.malinassur.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.cytech.malinassur.dao.ClaimDao;
import fr.cytech.malinassur.dao.ContractDao;
import fr.cytech.malinassur.dao.SupportingDocumentDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.AppUser;
import fr.cytech.malinassur.model.Claim;
import fr.cytech.malinassur.model.ClaimStatus;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.model.ContractStatus;
import fr.cytech.malinassur.model.SupportingDocument;
import jakarta.servlet.http.Part;

public class ClaimService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 Mo
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final ClaimDao claimDao = new ClaimDao();
    private final ContractDao contractDao = new ContractDao();
    private final SupportingDocumentDao documentDao = new SupportingDocumentDao();

    // =====================================================================
    // ESPACE CLIENT : DÉCLARATION & PIÈCES JUSTIFICATIVES
    // =====================================================================

    public Claim declareClaim(Long clientId, Long contractId, LocalDate incidentDate,
                              String claimType, String description, BigDecimal estimatedAmount) {
        if (clientId == null) throw new BusinessException("Utilisateur non authentifié.");
        if (contractId == null) throw new BusinessException("Veuillez sélectionner un contrat.");
        if (incidentDate == null || incidentDate.isAfter(LocalDate.now())) {
            throw new BusinessException("La date du sinistre est obligatoire et ne peut pas être dans le futur.");
        }
        if (claimType == null || claimType.isBlank()) {
            throw new BusinessException("Le type de sinistre est obligatoire.");
        }
        if (description == null || description.isBlank()) {
            throw new BusinessException("La description du sinistre est obligatoire.");
        }
        if (estimatedAmount == null || estimatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le montant estimé doit être strictement positif.");
        }

        Contract contract = contractDao.findById(contractId)
                .orElseThrow(() -> new BusinessException("Contrat introuvable."));

        if (!contract.getClientId().equals(clientId)) {
            throw new BusinessException("Ce contrat ne vous appartient pas.");
        }
        if (contract.getStatus() != ContractStatus.ACTIF) {
            throw new BusinessException("Impossible de déclarer un sinistre : le contrat n'est pas ACTIF.");
        }
        if (incidentDate.isBefore(contract.getStartDate()) || incidentDate.isAfter(contract.getEndDate())) {
            throw new BusinessException("La date du sinistre doit être comprise dans la période de validité du contrat.");
        }

        Claim claim = new Claim(
                nextClaimNumber(),
                incidentDate,
                claimType.trim(),
                description.trim(),
                estimatedAmount,
                contract
        );

        return claimDao.save(claim);
    }

    public SupportingDocument attachDocument(Long claimId, Part filePart, String uploadBaseDir) {
        if (filePart == null || filePart.getSize() == 0) {
            return null; // Aucun fichier joint
        }
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Le fichier dépasse la taille maximale autorisée (10 Mo).");
        }
        String mimeType = filePart.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException("Format de fichier non autorisé (PDF, JPEG, PNG ou WEBP uniquement).");
        }

        Claim claim = claimDao.findById(claimId)
                .orElseThrow(() -> new BusinessException("Sinistre introuvable."));

        String originalFileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String safeStorageName = UUID.randomUUID() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        try {
            Path uploadDir = Paths.get(uploadBaseDir, "claims");
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(safeStorageName);

            try (InputStream in = filePart.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            SupportingDocument doc = new SupportingDocument();
            doc.setFileName(originalFileName);
            doc.setStoragePath("uploads/claims/" + safeStorageName);
            doc.setMimeType(mimeType);
            doc.setFileSizeBytes(filePart.getSize());
            doc.setClaim(claim);

            return documentDao.save(doc);
        } catch (IOException e) {
            throw new BusinessException("Erreur technique lors de l'enregistrement de la pièce justificative.");
        }
    }

    // =====================================================================
    // ESPACE AGENT : INSTRUCTION & WORKFLOW DE STATUTS
    // =====================================================================

    public Claim takeUnderReview(Long claimId, Long agentId) {
        Claim claim = getById(claimId);
        if (claim.getStatus() != ClaimStatus.DECLARE) {
            throw new BusinessException("Seul un sinistre à l'état DECLARE peut être pris en charge.");
        }
        AppUser agentRef = new AppUser();
        agentRef.setId(agentId);
        claim.setAgent(agentRef);
        claim.setStatus(ClaimStatus.EN_ANALYSE);
        return claimDao.update(claim);
    }

    public Claim acceptClaim(Long claimId, Long agentId, BigDecimal indemnityAmount, String comment) {
        Claim claim = getById(claimId);
        if (claim.getStatus() != ClaimStatus.DECLARE && claim.getStatus() != ClaimStatus.EN_ANALYSE) {
            throw new BusinessException("Ce dossier ne peut plus être accepté dans son état actuel (" + claim.getStatus() + ").");
        }
        if (indemnityAmount == null || indemnityAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Le montant d'indemnisation validé doit être strictement positif.");
        }
        assignAgentIfMissing(claim, agentId);
        claim.setIndemnityAmount(indemnityAmount);
        claim.setAgentComment(comment != null ? comment.trim() : null);
        claim.setStatus(ClaimStatus.ACCEPTE);
        return claimDao.update(claim);
    }

    public Claim rejectClaim(Long claimId, Long agentId, String comment) {
        Claim claim = getById(claimId);
        if (claim.getStatus() != ClaimStatus.DECLARE && claim.getStatus() != ClaimStatus.EN_ANALYSE) {
            throw new BusinessException("Ce dossier ne peut plus être refusé dans son état actuel (" + claim.getStatus() + ").");
        }
        if (comment == null || comment.isBlank()) {
            throw new BusinessException("Un commentaire explicatif est obligatoire en cas de refus.");
        }
        assignAgentIfMissing(claim, agentId);
        claim.setIndemnityAmount(BigDecimal.ZERO);
        claim.setAgentComment(comment.trim());
        claim.setStatus(ClaimStatus.REFUSE);
        return claimDao.update(claim);
    }

    public Claim markAsIndemnified(Long claimId, Long agentId) {
        Claim claim = getById(claimId);
        if (claim.getStatus() != ClaimStatus.ACCEPTE) {
            throw new BusinessException("Seul un sinistre ACCEPTE peut être marqué comme INDEMNISE.");
        }
        assignAgentIfMissing(claim, agentId);
        claim.setStatus(ClaimStatus.INDEMNISE);
        return claimDao.update(claim);
    }

    // =====================================================================
    // CONSULTATION
    // =====================================================================

    public Claim getById(Long id) {
        return claimDao.findById(id)
                .orElseThrow(() -> new BusinessException("Sinistre introuvable."));
    }

    public Claim getWithDocuments(Long id) {
        return claimDao.findWithDocuments(id)
                .orElseThrow(() -> new BusinessException("Sinistre introuvable."));
    }

    public SupportingDocument getDocumentById(Long docId) {
        return documentDao.findById(docId)
                .orElseThrow(() -> new BusinessException("Pièce justificative introuvable."));
    }

    public List<Claim> findByClient(Long clientId) {
        return claimDao.findByClientId(clientId);
    }

    public List<Claim> findAll() {
        return claimDao.findAll();
    }

    public List<Claim> findByStatus(ClaimStatus status) {
        return claimDao.findByStatus(status);
    }

    private void assignAgentIfMissing(Claim claim, Long agentId) {
        if (claim.getAgent() == null && agentId != null) {
            AppUser agentRef = new AppUser();
            agentRef.setId(agentId);
            claim.setAgent(agentRef);
        }
    }

    private String nextClaimNumber() {
        int year = Year.now().getValue();
        long next = claimDao.countCreatedInYear(year) + 1;
        return String.format("SIN-%d-%06d", year, next);
    }
}