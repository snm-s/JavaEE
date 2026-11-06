package fr.cytech.malinassur.dao;

import java.util.List;

import fr.cytech.malinassur.model.Coverage;

public class CoverageDao extends BaseDao<Coverage> {

    public CoverageDao() {
        super(Coverage.class);
    }

    public List<Coverage> findActive() {
        return read(s -> s.createQuery(
                "from Coverage where active = true order by name", Coverage.class)
                .getResultList());
    }

    public boolean codeExists(String code, Long excludeId) {
        return read(s -> s.createQuery(
                "select count(c) from Coverage c where c.code = :code and (:id is null or c.id <> :id)", Long.class)
                .setParameter("code", code)
                .setParameter("id", excludeId)
                .uniqueResult() > 0);
    }

    /** Une garantie déjà utilisée dans un contrat ne peut pas être supprimée, seulement désactivée. */
    public boolean isUsedInContracts(Long coverageId) {
        return read(s -> s.createQuery(
                "select count(cc) from ContractCoverage cc where cc.coverage.id = :id", Long.class)
                .setParameter("id", coverageId)
                .uniqueResult() > 0);
    }
}
