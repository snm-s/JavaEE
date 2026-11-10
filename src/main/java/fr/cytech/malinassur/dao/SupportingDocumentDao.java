package fr.cytech.malinassur.dao;

import java.util.List;
import fr.cytech.malinassur.model.SupportingDocument;

public class SupportingDocumentDao extends BaseDao<SupportingDocument> {

    public SupportingDocumentDao() {
        super(SupportingDocument.class);
    }

    /** Récupère la liste des pièces justificatives associées à un sinistre. */
    public List<SupportingDocument> findByClaimId(Long claimId) {
        return read(session -> session.createQuery(
                        "FROM SupportingDocument d WHERE d.claim.id = :claimId ORDER BY d.uploadedAt ASC", SupportingDocument.class)
                .setParameter("claimId", claimId)
                .getResultList());
    }
}