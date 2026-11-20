package fr.cytech.malinassur.controller.contracts;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import fr.cytech.malinassur.exception.BusinessException;
import fr.cytech.malinassur.util.SessionUser;

/**
 * Petits outils partagés par les servlets du module Contrats & Produits :
 * lecture des champs de formulaire (avec vérification côté serveur),
 * messages "flash" et utilisateur connecté.
 */
final class WebHelper {

    static final String VIEWS = "/WEB-INF/views/";

    private WebHelper() {}

    // ------------------------------------------------------------------
    // Lecture des paramètres : toute saisie est vérifiée ICI, côté serveur
    // ------------------------------------------------------------------

    static String text(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    static Long optionalLong(HttpServletRequest req, String name) {
        String v = text(req, name);
        if (v == null) return null;
        try {
            return Long.valueOf(v);
        } catch (NumberFormatException e) {
            throw new BusinessException("Valeur invalide pour « " + name + " ».");
        }
    }

    static Long requiredLong(HttpServletRequest req, String name, String label) {
        Long v = optionalLong(req, name);
        if (v == null) throw new BusinessException(label + " est obligatoire.");
        return v;
    }

    static int requiredInt(HttpServletRequest req, String name, String label) {
        String v = text(req, name);
        if (v == null) throw new BusinessException(label + " est obligatoire.");
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + " doit être un nombre entier.");
        }
    }

    static LocalDate requiredDate(HttpServletRequest req, String name, String label) {
        String v = text(req, name);
        if (v == null) throw new BusinessException(label + " est obligatoire.");
        try {
            return LocalDate.parse(v); // format envoyé par <input type="date"> : 2026-11-17
        } catch (DateTimeParseException e) {
            throw new BusinessException(label + " n'est pas une date valide.");
        }
    }

    static BigDecimal requiredAmount(HttpServletRequest req, String name, String label) {
        String v = text(req, name);
        if (v == null) throw new BusinessException(label + " est obligatoire.");
        try {
            return new BigDecimal(v.replace(',', '.')).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new BusinessException(label + " doit être un montant (ex : 120.50).");
        }
    }

    static boolean checkbox(HttpServletRequest req, String name) {
        return req.getParameter(name) != null;
    }

    /** Toutes les cases cochées portant ce nom (ex : coverageIds). */
    static List<Long> longList(HttpServletRequest req, String name) {
        List<Long> ids = new ArrayList<>();
        String[] values = req.getParameterValues(name);
        if (values == null) return ids;
        for (String v : values) {
            try {
                ids.add(Long.valueOf(v));
            } catch (NumberFormatException e) {
                throw new BusinessException("Garantie invalide.");
            }
        }
        return ids;
    }

    // ------------------------------------------------------------------
    // Messages "flash" : affichés une fois sur la page suivante
    // ------------------------------------------------------------------

    static void flashSuccess(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flashSuccess", msg);
    }

    static void flashError(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flashError", msg);
    }

    static void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    static void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEWS + view).forward(req, resp);
    }

    // ------------------------------------------------------------------
    // Utilisateur connecté
    // ------------------------------------------------------------------

    /**
     * Id de l'utilisateur connecté, lu dans la session créée par {@code AuthServlet}.
     * Lève une {@link BusinessException} si la session a expiré : aucun identifiant
     * de test n'est utilisé en repli.
     */
    static Long currentUserId(HttpServletRequest req) {
        return SessionUser.requireId(req);
    }
}
