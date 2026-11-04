package fr.malinassur.service;

import java.math.BigDecimal;
import java.util.List;

import fr.malinassur.dao.CoverageDao;
import fr.malinassur.dao.InsuranceTypeDao;
import fr.malinassur.exception.BusinessException;
import fr.malinassur.model.Coverage;
import fr.malinassur.model.InsuranceType;
import fr.malinassur.model.InsuranceTypeCoverage;

/**
 * Gestion du "catalogue" : types d'assurance + garanties + leurs liens.
 * Toutes les vérifications sont faites ici, côté serveur.
 */
public class CatalogService {

    private final InsuranceTypeDao typeDao = new InsuranceTypeDao();
    private final CoverageDao coverageDao = new CoverageDao();

    // ---------------- Types d'assurance (admin) ----------------

    public List<InsuranceType> allTypes()    { return typeDao.findAll(); }
    public List<InsuranceType> activeTypes() { return typeDao.findActive(); }

    public InsuranceType getType(Long id) {
        return typeDao.findById(id).orElseThrow(() -> new BusinessException("Type d'assurance introuvable."));
    }

    public InsuranceType createType(String code, String name, String description, BigDecimal basePrice) {
        validateType(code, name, basePrice);
        if (typeDao.findByCode(code.trim().toUpperCase()).isPresent())
            throw new BusinessException("Ce code de type existe déjà.");
        return typeDao.save(new InsuranceType(code.trim().toUpperCase(), name.trim(), description, basePrice));
    }

    public InsuranceType updateType(Long id, String name, String description, BigDecimal basePrice, boolean active) {
        InsuranceType t = getType(id);
        validateType(t.getCode(), name, basePrice);
        t.setName(name.trim());
        t.setDescription(description);
        t.setBasePrice(basePrice);
        t.setActive(active);
        return typeDao.update(t);
    }

    public List<InsuranceTypeCoverage> offeredCoverages(Long typeId) {
        return typeDao.findOfferedCoverages(typeId);
    }

    public void offerCoverage(Long typeId, Long coverageId, boolean isDefault, BigDecimal additionalPremium) {
        if (additionalPremium == null || additionalPremium.signum() < 0)
            throw new BusinessException("Le supplément doit être positif ou nul.");
        getType(typeId);
        getCoverage(coverageId);
        typeDao.offerCoverage(typeId, coverageId, isDefault, additionalPremium);
    }

    public void withdrawCoverage(Long typeId, Long coverageId) {
        typeDao.withdrawCoverage(typeId, coverageId);
    }

    // ---------------- Garanties (agent) ----------------

    public List<Coverage> allCoverages()    { return coverageDao.findAll(); }
    public List<Coverage> activeCoverages() { return coverageDao.findActive(); }

    public Coverage getCoverage(Long id) {
        return coverageDao.findById(id).orElseThrow(() -> new BusinessException("Garantie introuvable."));
    }

    public Coverage createCoverage(String code, String name, String description, String coverageType) {
        validateCoverage(code, name);
        String c = code.trim().toUpperCase();
        if (coverageDao.codeExists(c, null)) throw new BusinessException("Ce code de garantie existe déjà.");
        return coverageDao.save(new Coverage(c, name.trim(), description, coverageType));
    }

    public Coverage updateCoverage(Long id, String name, String description, String coverageType, boolean active) {
        Coverage cov = getCoverage(id);
        validateCoverage(cov.getCode(), name);
        cov.setName(name.trim());
        cov.setDescription(description);
        cov.setCoverageType(coverageType);
        cov.setActive(active);
        return coverageDao.update(cov);
    }

    /** Suppression physique seulement si jamais utilisée ; sinon on la désactive. */
    public boolean deleteCoverage(Long id) {
        if (coverageDao.isUsedInContracts(id)) {
            Coverage cov = getCoverage(id);
            cov.setActive(false);
            coverageDao.update(cov);
            return false;   // désactivée
        }
        coverageDao.deleteById(id);
        return true;        // supprimée
    }

    // ---------------- Validations ----------------

    private void validateType(String code, String name, BigDecimal basePrice) {
        if (code == null || !code.trim().matches("[A-Za-z_]{2,20}"))
            throw new BusinessException("Code invalide (2 à 20 lettres, ex : AUTO).");
        if (name == null || name.isBlank() || name.length() > 100)
            throw new BusinessException("Le nom est obligatoire (100 caractères max).");
        if (basePrice == null || basePrice.signum() <= 0)
            throw new BusinessException("Le prix de base doit être strictement positif.");
    }

    private void validateCoverage(String code, String name) {
        if (code == null || !code.trim().matches("[A-Za-z_]{2,30}"))
            throw new BusinessException("Code invalide (2 à 30 lettres, ex : VOL).");
        if (name == null || name.isBlank() || name.length() > 100)
            throw new BusinessException("Le nom est obligatoire (100 caractères max).");
    }
}
