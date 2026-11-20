<%-- =====================================================================
     Gestion des garanties (espace AGENT ; aussi utilisée par admin/guarantees.jsp)
     Servlet : GuaranteeServlet  (GET /agent/guarantees  |  /admin/guarantees)
     ===================================================================== --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="base" value="${ctx}/${role}/guarantees"/>
<c:set var="editing" value="${not empty editCoverage}"/>

<div class="ct-page-head">
    <div>
        <h1>Garanties</h1>
        <p class="ct-subtitle">Les couvertures proposées dans les contrats (vol, incendie, assistance...).</p>
    </div>
</div>
<%@ include file="/WEB-INF/views/includes/contracts/subnav.jsp" %>

<div class="ct-grid-2">
    <%-- ---------- Formulaire ajout / modification ---------- --%>
    <section class="ct-panel">
        <h2>${editing ? 'Modifier la garantie' : 'Nouvelle garantie'}</h2>
        <form method="post" action="${base}/save">
            <c:if test="${editing}"><input type="hidden" name="id" value="${editCoverage.id}"></c:if>
            <div class="ct-form-row">
                <div class="ct-field">
                    <label for="code">Code *</label>
                    <input type="text" id="code" name="code" required pattern="[A-Za-z_]{2,30}"
                           placeholder="ex : BRIS_GLACE" value="<c:out value='${editCoverage.code}'/>" ${editing ? 'readonly' : ''}>
                    <div class="ct-hint">Lettres et _ uniquement. Non modifiable ensuite.</div>
                </div>
                <div class="ct-field">
                    <label for="name">Nom *</label>
                    <input type="text" id="name" name="name" required maxlength="100" value="<c:out value='${editCoverage.name}'/>">
                </div>
            </div>
            <div class="ct-form-row">
                <div class="ct-field">
                    <label for="coverageType">Catégorie</label>
                    <select id="coverageType" name="coverageType">
                        <c:forEach var="cat" items="DOMMAGE,RESPONSABILITE,ASSISTANCE">
                            <option value="${cat}" ${cat == editCoverage.coverageType ? 'selected' : ''}>${cat}</option>
                        </c:forEach>
                    </select>
                </div>
                <c:if test="${editing}">
                    <div class="ct-field">
                        <label>&nbsp;</label>
                        <label class="ct-check"><input type="checkbox" name="active" ${editCoverage.active ? 'checked' : ''}> Garantie active</label>
                    </div>
                </c:if>
            </div>
            <div class="ct-field" style="margin-bottom:14px">
                <label for="description">Description</label>
                <textarea id="description" name="description" rows="3"><c:out value="${editCoverage.description}"/></textarea>
            </div>
            <div class="ct-actions">
                <button type="submit" class="ct-btn">${editing ? 'Enregistrer' : 'Ajouter la garantie'}</button>
                <c:if test="${editing}"><a href="${base}" class="ct-btn ct-btn-light">Annuler</a></c:if>
            </div>
        </form>
    </section>

    <%-- ---------- Liste ---------- --%>
    <section class="ct-panel">
        <h2>Toutes les garanties</h2>
        <table class="ct-table">
            <thead><tr><th>Code</th><th>Nom</th><th>Catégorie</th><th>État</th><th></th></tr></thead>
            <tbody>
            <c:forEach var="g" items="${coverages}">
                <tr>
                    <td><strong>${g.code}</strong></td>
                    <td><c:out value="${g.name}"/><br><small class="ct-hint"><c:out value="${g.description}"/></small></td>
                    <td>${g.coverageType}</td>
                    <td><span class="ct-badge ${g.active ? 'ct-ACTIF' : 'ct-tag-off'}">${g.active ? 'Active' : 'Inactive'}</span></td>
                    <td>
                        <div class="ct-actions">
                            <a href="${base}?edit=${g.id}" class="ct-btn ct-btn-light ct-btn-sm">Modifier</a>
                            <form method="post" action="${base}/delete" class="ct-inline"
                                  onsubmit="return confirm('Supprimer cette garantie ? (Si elle est utilisée dans un contrat, elle sera seulement désactivée.)');">
                                <input type="hidden" name="id" value="${g.id}">
                                <button class="ct-btn ct-btn-danger ct-btn-sm">Supprimer</button>
                            </form>
                        </div>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty coverages}"><tr><td colspan="5" class="ct-empty">Aucune garantie.</td></tr></c:if>
            </tbody>
        </table>
    </section>
</div>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
