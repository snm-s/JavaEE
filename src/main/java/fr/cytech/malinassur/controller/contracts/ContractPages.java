package fr.cytech.malinassur.controller.contracts;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import fr.cytech.malinassur.dao.UserLookupDao;
import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.model.Contract;
import fr.cytech.malinassur.model.ContractCoverage;
import fr.cytech.malinassur.model.ContractStatus;
import fr.cytech.malinassur.model.InsuranceType;
import fr.cytech.malinassur.model.InsuranceTypeCoverage;
import fr.cytech.malinassur.service.CatalogService;
import fr.cytech.malinassur.service.ContractService;

/**
 * Prépare les données affichées par les JSP des contrats.
 * Partagé par l'espace agent et l'espace admin (factorisation du code).
 */
final class ContractPages {

    private ContractPages() {}

    /** Liste + recherche multicritère. Paramètres : q, status, typeId, clientId. */
    static void prepareList(HttpServletRequest req, ContractService contracts, CatalogService catalog,
                            UserLookupDao users) {
        contracts.expireOutdatedContracts(); // met à jour les contrats dont la date de fin est passée

        String q = WebHelper.text(req, "q");
        Long typeId = WebHelper.optionalLong(req, "typeId");
        Long clientId = WebHelper.optionalLong(req, "clientId");
        ContractStatus status = null;
        String st = WebHelper.text(req, "status");
        if (st != null) {
            try {
                status = ContractStatus.valueOf(st);
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Statut inconnu : " + st);
            }
        }

        req.setAttribute("contracts", contracts.search(q, clientId, typeId, status));
        req.setAttribute("types", catalog.allTypes());
        req.setAttribute("statuses", ContractStatus.values());
        req.setAttribute("clients", users.mapByRole("CLIENT"));
        // pour garder les filtres remplis après la recherche
        req.setAttribute("fQ", q);
        req.setAttribute("fTypeId", typeId);
        req.setAttribute("fClientId", clientId);
        req.setAttribute("fStatus", st);
    }

    /** Données du formulaire de création / modification. */
    static void prepareForm(HttpServletRequest req, CatalogService catalog, UserLookupDao users) {
        List<InsuranceType> types = catalog.activeTypes();
        Map<Long, List<InsuranceTypeCoverage>> offers = new LinkedHashMap<>();
        for (InsuranceType t : types) {
            offers.put(t.getId(), catalog.offeredCoverages(t.getId()));
        }
        req.setAttribute("types", types);
        req.setAttribute("offers", offers);
        req.setAttribute("clients", users.findByRole("CLIENT"));
    }

    /** Ids des garanties déjà choisies (pour pré-cocher les cases en modification). */
    static Set<Long> selectedCoverageIds(Contract c) {
        Set<Long> ids = new HashSet<>();
        for (ContractCoverage cc : c.getCoverages()) ids.add(cc.getCoverage().getId());
        return ids;
    }

    /** Fiche détaillée d'un contrat + boutons autorisés selon son statut. */
    static void prepareDetails(HttpServletRequest req, Long id, ContractService contracts, UserLookupDao users) {
        Contract c = contracts.get(id);
        ContractStatus s = c.getStatus();
        req.setAttribute("contract", c);
        req.setAttribute("client", users.findSummaryById(c.getClientId()).orElse(null));
        req.setAttribute("agent", users.findSummaryById(c.getAgentId()).orElse(null));
        req.setAttribute("agents", users.findByRole("AGENT"));
        req.setAttribute("canActivate", contracts.canChange(s, ContractStatus.ACTIF));
        req.setAttribute("canSuspend", contracts.canChange(s, ContractStatus.SUSPENDU));
        req.setAttribute("canTerminate", contracts.canChange(s, ContractStatus.RESILIE));
        req.setAttribute("canRenew", contracts.canRenew(s));
        req.setAttribute("canEdit", s != ContractStatus.RESILIE);
    }
}
