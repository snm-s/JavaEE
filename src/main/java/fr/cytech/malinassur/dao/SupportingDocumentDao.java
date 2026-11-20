package fr.cytech.malinassur.dao;

import java.util.List;
import java.util.Optional;

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

    /**
     * Charge une pièce avec son sinistre et le contrat associé, afin de vérifier
     * la propriété du document sans provoquer de {@code LazyInitializationException}
     * après la fermeture de la session Hibernate.
     */
    public Optional<SupportingDocument> findByIdWithClaim(Long docId) {
        return read(session -> session.createQuery(
                        "SELECT d FROM SupportingDocument d "
                                + "JOIN FETCH d.claim c JOIN FETCH c.contract "
                                + "WHERE d.id = :id", SupportingDocument.class)
                .setParameter("id", docId)
                .uniqueResultOptional());
    }
}