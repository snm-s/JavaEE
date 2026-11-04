package fr.malinassur.dao;

import java.util.List;
import java.util.Optional;

import fr.malinassur.model.InsuranceType;
import fr.malinassur.model.InsuranceTypeCoverage;

public class InsuranceTypeDao extends BaseDao<InsuranceType> {

    public InsuranceTypeDao() {
        super(InsuranceType.class);
    }

    /** Types proposés aux clients (les types désactivés par l'admin sont cachés). */
    public List<InsuranceType> findActive() {
        return read(s -> s.createQuery(
                "from InsuranceType where active = true order by name", InsuranceType.class)
                .getResultList());
    }

    public Optional<InsuranceType> findByCode(String code) {
        return read(s -> s.createQuery(
                "from InsuranceType where code = :code", InsuranceType.class)
                .setParameter("code", code)
                .uniqueResultOptional());
    }

    /** Le catalogue de garanties d'un type (avec isDefault et le supplément de prix). */
    public List<InsuranceTypeCoverage> findOfferedCoverages(Long typeId) {
        return read(s -> s.createQuery(
                "select itc from InsuranceTypeCoverage itc join fetch itc.coverage c " +
                "where itc.insuranceType.id = :id and c.active = true order by itc.isDefault desc, c.name",
                InsuranceTypeCoverage.class)
                .setParameter("id", typeId)
                .getResultList());
    }

    /** Ajoute ou met à jour une garantie dans le catalogue d'un type. */
    public void offerCoverage(Long typeId, Long coverageId, boolean isDefault, java.math.BigDecimal additionalPremium) {
        inTransactionVoid(s -> {
            InsuranceTypeCoverage existing = s.createQuery(
                    "from InsuranceTypeCoverage where insuranceType.id = :t and coverage.id = :c",
                    InsuranceTypeCoverage.class)
                    .setParameter("t", typeId).setParameter("c", coverageId)
                    .uniqueResult();
            if (existing != null) {
                existing.setDefault(isDefault);
                existing.setAdditionalPremium(additionalPremium);
            } else {
                InsuranceType type = s.get(InsuranceType.class, typeId);
                fr.malinassur.model.Coverage cov = s.get(fr.malinassur.model.Coverage.class, coverageId);
                s.persist(new InsuranceTypeCoverage(type, cov, isDefault, additionalPremium));
            }
        });
    }

    /** Retire une garantie du catalogue d'un type (les contrats déjà signés gardent la leur). */
    public void withdrawCoverage(Long typeId, Long coverageId) {
        inTransactionVoid(s -> s.createMutationQuery(
                "delete from InsuranceTypeCoverage where insuranceType.id = :t and coverage.id = :c")
                .setParameter("t", typeId).setParameter("c", coverageId)
                .executeUpdate());
    }
}
