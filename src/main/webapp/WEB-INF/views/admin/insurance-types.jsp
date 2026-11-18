<%-- =====================================================================
     Espace ADMIN : types d'assurance + catalogue de garanties de chaque type
     Servlet : InsuranceTypeServlet (GET /admin/insurance-types)
     Consigne : « ajouter facilement un nouveau type d'assurance sans modifier le code »
     → ici, un nouveau type = un formulaire rempli, aucune ligne de Java à écrire.
     ===================================================================== --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="base" value="${ctx}/admin/insurance-types"/>
<c:set var="editing" value="${not empty editType}"/>

<div class="ct-page-head">
    <div>
        <h1>Types d'assurance</h1>
        <p class="ct-subtitle">Créez un produit, fixez son prix de base, puis composez son catalogue de garanties.</p>
    </div>
</div>
<%@ include file="/WEB-INF/views/includes/contracts/subnav.jsp" %>

<%-- ---------- Formulaire type ---------- --%>
<section class="ct-panel" id="form">
    <h2>${editing ? 'Modifier le type' : 'Nouveau type d\'assurance'}</h2>
    <form method="post" action="${base}/save">
        <c:if test="${editing}"><input type="hidden" name="id" value="${editType.id}"></c:if>
        <div class="ct-form-row">
            <div class="ct-field">
                <label for="code">Code *</label>
                <input type="text" id="code" name="code" required pattern="[A-Za-z_]{2,20}" placeholder="ex : ANIMAUX"
                       value="<c:out value='${editType.code}'/>" ${editing ? 'readonly' : ''}>
            </div>
            <div class="ct-field">
                <label for="name">Nom *</label>
                <input type="text" id="name" name="name" required maxlength="100" placeholder="ex : Assurance animaux"
                       value="<c:out value='${editType.name}'/>">
            </div>
            <div class="ct-field">
                <label for="basePrice">Prix de base (€ / an) *</label>
                <input type="number" id="basePrice" name="basePrice" required min="0.01" step="0.01"
                       value="${editType.basePrice}">
            </div>
            <c:if test="${editing}">
                <div class="ct-field">
                    <label>&nbsp;</label>
                    <label class="ct-check"><input type="checkbox" name="active" ${editType.active ? 'checked' : ''}> Proposé aux clients</label>
                </div>
            </c:if>
        </div>
        <div class="ct-field" style="margin-bottom:14px">
            <label for="description">Description</label>
            <textarea id="description" name="description" rows="2"><c:out value="${editType.description}"/></textarea>
        </div>
        <div class="ct-actions">
            <button type="submit" class="ct-btn">${editing ? 'Enregistrer' : 'Créer le type'}</button>
            <c:if test="${editing}"><a href="${base}" class="ct-btn ct-btn-light">Annuler</a></c:if>
        </div>
    </form>
</section>

<%-- ---------- Un panneau par type, avec son catalogue ---------- --%>
<c:forEach var="t" items="${types}">
    <section class="ct-panel">
        <div class="ct-page-head" style="margin-bottom:10px">
            <div>
                <h2 style="margin:0"><c:out value="${t.name}"/>
                    <span class="ct-badge ${t.active ? 'ct-ACTIF' : 'ct-tag-off'}">${t.active ? 'Proposé' : 'Désactivé'}</span>
                </h2>
                <p class="ct-subtitle">${t.code} · prix de base <strong>${t.basePrice} €</strong> · <c:out value="${t.description}"/></p>
            </div>
            <a href="${base}?edit=${t.id}#form" class="ct-btn ct-btn-light ct-btn-sm">Modifier</a>
        </div>

        <table class="ct-table">
            <thead><tr><th>Garantie</th><th>Inclusion</th><th class="ct-num">Supplément</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="o" items="${offers[t.id]}">
                <tr>
                    <td><c:out value="${o.coverage.name}"/></td>
                    <td>
                        <span class="ct-badge ${o['default'] ? 'ct-tag-default' : 'ct-tag-option'}">${o['default'] ? 'Incluse' : 'En option'}</span>
                    </td>
                    <td class="ct-num">${o.additionalPremium} €</td>
                    <td>
                        <form method="post" action="${base}/withdraw" class="ct-inline"
                              onsubmit="return confirm('Retirer cette garantie du catalogue ?');">
                            <input type="hidden" name="typeId" value="${t.id}">
                            <input type="hidden" name="coverageId" value="${o.coverage.id}">
                            <button class="ct-btn ct-btn-danger ct-btn-sm">Retirer</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty offers[t.id]}"><tr><td colspan="4" class="ct-empty">Aucune garantie : ajoutez-en ci-dessous.</td></tr></c:if>
            </tbody>
        </table>

        <%-- Ajouter / modifier une garantie dans le catalogue de ce type --%>
        <form method="post" action="${base}/offer" style="margin-top:14px">
            <input type="hidden" name="typeId" value="${t.id}">
            <div class="ct-form-row" style="align-items:end">
                <div class="ct-field">
                    <label>Garantie</label>
                    <select name="coverageId" required>
                        <c:forEach var="g" items="${coverages}">
                            <option value="${g.id}">
                                <c:out value="${g.name}"/>${offeredIds[t.id].contains(g.id) ? ' (déjà au catalogue : mise à jour)' : ''}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="ct-field">
                    <label>Supplément (€)</label>
                    <input type="number" name="additionalPremium" min="0" step="0.01" value="0" required>
                </div>
                <div class="ct-field">
                    <label class="ct-check"><input type="checkbox" name="isDefault"> Incluse d'office</label>
                </div>
                <div><button class="ct-btn ct-btn-sage">Ajouter au catalogue</button></div>
            </div>
        </form>
    </section>
</c:forEach>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
