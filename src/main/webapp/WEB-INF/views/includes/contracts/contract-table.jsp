<%-- =====================================================================
     Recherche + tableau des contrats (partage agent / admin).
     Lien vers la fiche : agent -> /agent/contracts/view?id= ; admin -> /admin/contracts?id=
     ===================================================================== --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="detailUrl" value="${role == 'admin' ? ctx.concat('/admin/contracts?id=') : ctx.concat('/agent/contracts/view?id=')}"/>

<section class="ct-panel">
    <form method="get" action="${ctx}/${role}/contracts">
        <div class="ct-form-row">
            <div class="ct-field">
                <label for="q">Numéro de contrat</label>
                <input type="text" id="q" name="q" placeholder="ex : CTR-2026" value="<c:out value='${fQ}'/>">
            </div>
            <div class="ct-field">
                <label for="clientId">Client</label>
                <select id="clientId" name="clientId">
                    <option value="">Tous les clients</option>
                    <c:forEach var="entry" items="${clients}">
                        <option value="${entry.key}" ${entry.key == fClientId ? 'selected' : ''}><c:out value="${entry.value.fullName}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="ct-field">
                <label for="typeId">Type d'assurance</label>
                <select id="typeId" name="typeId">
                    <option value="">Tous les types</option>
                    <c:forEach var="t" items="${types}">
                        <option value="${t.id}" ${t.id == fTypeId ? 'selected' : ''}><c:out value="${t.name}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="ct-field">
                <label for="status">Statut</label>
                <select id="status" name="status">
                    <option value="">Tous les statuts</option>
                    <c:forEach var="s" items="${statuses}">
                        <option value="${s}" ${s == fStatus ? 'selected' : ''}>${s}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="ct-actions">
            <button type="submit" class="ct-btn">Rechercher</button>
            <a href="${ctx}/${role}/contracts" class="ct-btn ct-btn-light">Réinitialiser</a>
        </div>
    </form>
</section>

<section class="ct-panel">
    <table class="ct-table">
        <thead>
        <tr>
            <th>N° contrat</th><th>Client</th><th>Type</th><th>Période</th>
            <th class="ct-num">Prime / an</th><th>Statut</th><th></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="ct" items="${contracts}">
            <tr>
                <td><strong>${ct.contractNumber}</strong></td>
                <td>
                    <c:choose>
                        <c:when test="${not empty clients[ct.clientId]}"><c:out value="${clients[ct.clientId].fullName}"/></c:when>
                        <c:otherwise>Client n°${ct.clientId}</c:otherwise>
                    </c:choose>
                </td>
                <td><c:out value="${ct.insuranceType.name}"/></td>
                <td>${ct.startDate} → ${ct.endDate}</td>
                <td class="ct-num">${ct.premiumAmount} €</td>
                <td><c:set var="st" value="${ct.status}"/><%@ include file="/WEB-INF/views/includes/contracts/status-badge.jsp" %></td>
                <td><a href="${detailUrl}${ct.id}" class="ct-btn ct-btn-light ct-btn-sm">Voir</a></td>
            </tr>
        </c:forEach>
        <c:if test="${empty contracts}">
            <tr><td colspan="7" class="ct-empty">Aucun contrat ne correspond à la recherche.</td></tr>
        </c:if>
        </tbody>
    </table>
</section>
