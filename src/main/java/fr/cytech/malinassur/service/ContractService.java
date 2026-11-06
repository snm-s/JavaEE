package fr.cytech.malinassur.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.cytech.malinassur.dao.ContractDao;
import fr.cytech.malinassur.dao.InsuranceTypeDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.model.ContractStatus;
import fr.cytech.malinassur.model.InsuranceType;
import fr.cytech.malinassur.model.InsuranceTypeCoverage;

/**
 * Toute la LOGIQUE MÉTIER des contrats : création, calcul de prime, cycle de vie.
 * La servlet ne fait que lire le formulaire et appeler ces méthodes.
 *
 * Cycle de vie autorisé :
 *   EN_ATTENTE → ACTIF, RESILIE
 *   ACTIF      → SUSPENDU, RESILIE, EXPIRE (automatique à la date de fin)
 *   SUSPENDU   → ACTIF, RESILIE
 *   EXPIRE     → ACTIF (renouvellement)
 *   RESILIE    → (fin, plus rien possible)
 */
public class ContractService {

    private static final Map<ContractStatus, Set<ContractStatus>> TRANSITIONS = Map.of(
            ContractStatus.EN_ATTENTE, Set.of(ContractStatus.ACTIF, ContractStatus.RESILIE),
            ContractStatus.ACTIF,      Set.of(ContractStatus.SUSPENDU, ContractStatus.RESILIE, ContractStatus.EXPIRE),
            ContractStatus.SUSPENDU,   Set.of(ContractStatus.ACTIF, ContractStatus.RESILIE),
            ContractStatus.EXPIRE,     Set.of(ContractStatus.ACTIF),
            ContractStatus.RESILIE,    Set.of()
    );

    private final ContractDao contractDao = new ContractDao();
    private final InsuranceTypeDao typeDao = new InsuranceTypeDao();

    // =====================================================================
    // CRÉATION
    // =====================================================================

    /**
     * Crée un contrat EN_ATTENTE.
     * - les garanties "par défaut" du type sont toujours ajoutées ;
     * - les garanties optionnelles cochées par l'agent sont ajoutées en plus ;
     * - la prime est calculée automatiquement.
     */
    public Contract create(Long clientId, Long typeId, List<Long> optionalCoverageIds,
                           LocalDate startDate, int durationMonths, Long agentId) {

        if (clientId == null) throw new BusinessException("Le client est obligatoire.");
        if (startDate == null) throw new BusinessException("La date de début est obligatoire.");
        if (durationMonths < 1 || durationMonths > 60)
            throw new BusinessException("La durée doit être comprise entre 1 et 60 mois.");

        InsuranceType type = typeDao.findById(typeId)
                .orElseThrow(() -> new BusinessException("Type d'assurance introuvable."));
        if (!type.isActive()) throw new BusinessException("Ce type d'assurance n'est plus proposé.");

        Contract c = new Contract();
        c.setContractNumber(nextContractNumber());
        c.setClientId(clientId);
        c.setAgentId(agentId);
        c.setInsuranceType(type);
        c.setStartDate(startDate);
        c.setEndDate(startDate.plusMonths(durationMonths).minusDays(1));
        c.setStatus(ContractStatus.EN_ATTENTE);

        applyCoverages(c, typeId, optionalCoverageIds);
        c.recalculatePremium();

        return contractDao.save(c);
    }

    // =====================================================================
    // MODIFICATION
    // =====================================================================

    /** Modifie dates + garanties. Interdit si le contrat est résilié. */
    public Contract update(Long contractId, LocalDate startDate, LocalDate endDate, List<Long> optionalCoverageIds) {
        Contract c = get(contractId);
        if (c.getStatus() == ContractStatus.RESILIE)
            throw new BusinessException("Un contrat résilié ne peut plus être modifié.");
        if (startDate == null || endDate == null || !endDate.isAfter(startDate))
            throw new BusinessException("La date de fin doit être après la date de début.");

        c.setStartDate(startDate);
        c.setEndDate(endDate);
        applyCoverages(c, c.getInsuranceType().getId(), optionalCoverageIds);
        c.recalculatePremium();

        return contractDao.update(c);
    }

    public Contract assignAgent(Long contractId, Long agentId) {
        Contract c = get(contractId);
        c.setAgentId(agentId);
        return contractDao.update(c);
    }

    // =====================================================================
    // CYCLE DE VIE
    // =====================================================================

    public Contract activate(Long id)  { return changeStatus(id, ContractStatus.ACTIF); }
    public Contract suspend(Long id)   { return changeStatus(id, ContractStatus.SUSPENDU); }
    public Contract terminate(Long id) { return changeStatus(id, ContractStatus.RESILIE); }

    /** Renouvelle pour 12 mois à partir de la fin actuelle (ou d'aujourd'hui si déjà expiré). */
    public Contract renew(Long id) {
        Contract c = get(id);
        if (c.getStatus() != ContractStatus.ACTIF && c.getStatus() != ContractStatus.EXPIRE)
            throw new BusinessException("Seul un contrat actif ou expiré peut être renouvelé.");

        LocalDate from = c.getEndDate().isBefore(LocalDate.now()) ? LocalDate.now() : c.getEndDate();
        c.setEndDate(from.plusYears(1));
        c.setStatus(ContractStatus.ACTIF);
        return contractDao.update(c);
    }

    /** À appeler au démarrage de l'appli ou avant d'afficher une liste. */
    public int expireOutdatedContracts() {
        return contractDao.expireOutdated(LocalDate.now());
    }

    // =====================================================================
    // CONSULTATION
    // =====================================================================

    public Contract get(Long id) {
        return contractDao.findById(id)
                .orElseThrow(() -> new BusinessException("Contrat introuvable."));
    }

    public List<Contract> findByClient(Long clientId) {
        return contractDao.findByClient(clientId);
    }

    public List<Contract> search(String number, Long clientId, Long typeId, ContractStatus status) {
        return contractDao.search(number, clientId, typeId, status);
    }

    /** Aperçu de la prime AVANT création (pour l'afficher dans le formulaire). */
    public BigDecimal previewPremium(Long typeId, List<Long> optionalCoverageIds) {
        InsuranceType type = typeDao.findById(typeId)
                .orElseThrow(() -> new BusinessException("Type d'assurance introuvable."));
        BigDecimal total = type.getBasePrice();
        for (InsuranceTypeCoverage itc : typeDao.findOfferedCoverages(typeId)) {
            boolean chosen = itc.isDefault()
                    || (optionalCoverageIds != null && optionalCoverageIds.contains(itc.getCoverage().getId()));
            if (chosen) total = total.add(itc.getAdditionalPremium());
        }
        return total;
    }

    // =====================================================================
    // OUTILS PRIVÉS
    // =====================================================================

    private Contract changeStatus(Long id, ContractStatus target) {
        Contract c = get(id);
        if (!TRANSITIONS.get(c.getStatus()).contains(target)) {
            throw new BusinessException("Impossible de passer un contrat de "
                    + c.getStatus() + " à " + target + ".");
        }
        c.setStatus(target);
        return contractDao.update(c);
    }

    /** Ajoute les garanties par défaut + celles choisies, en vérifiant qu'elles sont bien au catalogue du type. */
    private void applyCoverages(Contract c, Long typeId, List<Long> optionalCoverageIds) {
        List<InsuranceTypeCoverage> catalog = typeDao.findOfferedCoverages(typeId);
        Set<Long> allowed = catalog.stream().map(i -> i.getCoverage().getId()).collect(Collectors.toSet());

        if (optionalCoverageIds != null) {
            for (Long id : optionalCoverageIds) {
                if (!allowed.contains(id))
                    throw new BusinessException("La garantie n°" + id + " n'est pas proposée pour ce type d'assurance.");
            }
        }
        // 1) liste des garanties voulues = celles par défaut + celles cochées
        List<InsuranceTypeCoverage> chosen = catalog.stream()
                .filter(i -> i.isDefault()
                        || (optionalCoverageIds != null && optionalCoverageIds.contains(i.getCoverage().getId())))
                .toList();
        Set<Long> chosenIds = chosen.stream().map(i -> i.getCoverage().getId()).collect(Collectors.toSet());

        // 2) on retire celles qui ne sont plus voulues (cas de la modification)
        c.getCoverages().removeIf(cc -> !chosenIds.contains(cc.getCoverage().getId()));

        // 3) on ajoute celles qui manquent, avec le prix figé au moment T
        for (InsuranceTypeCoverage itc : chosen) {
            if (!c.hasCoverage(itc.getCoverage().getId())) {
                c.addCoverage(itc.getCoverage(), itc.getAdditionalPremium());
            }
        }
    }

    /** CTR-2026-000001, CTR-2026-000002... */
    private String nextContractNumber() {
        int year = Year.now().getValue();
        long next = contractDao.countCreatedInYear(year) + 1;
        return String.format("CTR-%d-%06d", year, next);
    }
}
