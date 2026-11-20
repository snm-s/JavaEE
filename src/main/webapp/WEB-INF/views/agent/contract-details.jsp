<%-- =====================================================================
     Fiche d'un contrat (espace AGENT, et ADMIN en lecture seule)
     Servlets : AgentContractServlet (GET /agent/contracts/view?id=)
                AdminContractServlet (GET /admin/contracts?id=)  → readOnly = true
     Les boutons affichés dépendent du statut (canActivate, canSuspend... calculés par la servlet).
     ===================================================================== --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="action" value="${ctx}/agent/contracts/status"/>

<div class="ct-page-head">
    <div>
        <h1>Contrat ${contract.contractNumber}</h1>
        <p class="ct-subtitle"><c:out value="${contract.insuranceType.name}"/> ·
            <c:set var="st" value="${contract.status}"/><%@ include file="/WEB-INF/views/includes/contracts/status-badge.jsp" %>
        </p>
    </div>
    <a href="${ctx}/${role}/contracts" class="ct-btn ct-btn-light">← Retour à la liste</a>
</div>
<%@ include file="/WEB-INF/views/includes/contracts/subnav.jsp" %>

<%-- ---------- Actions (agent uniquement) ---------- --%>
<c:if test="${not readOnly}">
    <section class="ct-panel">
        <h2>Actions</h2>
        <div class="ct-actions">
            <c:if test="${canActivate}">
                <form method="post" action="${action}" class="ct-inline">
                    <input type="hidden" name="id" value="${contract.id}"><input type="hidden" name="op" value="activate">
                    <button class="ct-btn ct-btn-sage">${contract.status == 'SUSPENDU' ? 'Réactiver' : 'Activer'}</button>
                </form>
            </c:if>
            <c:if test="${canSuspend}">
                <form method="post" action="${action}" class="ct-inline">
                    <input type="hidden" name="id" value="${contract.id}"><input type="hidden" name="op" value="suspend">
                    <button class="ct-btn ct-btn-light">Suspendre</button>
                </form>
            </c:if>
            <c:if test="${canRenew}">
                <form method="post" action="${action}" class="ct-inline">
                    <input type="hidden" name="id" value="${contract.id}"><input type="hidden" name="op" value="renew">
                    <button class="ct-btn ct-btn-light">Renouveler (+1 an)</button>
                </form>
            </c:if>
            <c:if test="${canEdit}">
                <a href="${ctx}/agent/contracts/edit?id=${contract.id}" class="ct-btn ct-btn-light">Modifier</a>
            </c:if>
            <c:if test="${canTerminate}">
                <form method="post" action="${action}" class="ct-inline"
                      onsubmit="return confirm('Résilier définitivement ce contrat ?');">
                    <input type="hidden" name="id" value="${contract.id}"><input type="hidden" name="op" value="terminate">
                    <button class="ct-btn ct-btn-danger">Résilier</button>
                </form>
            </c:if>
            <c:if test="${not canActivate and not canSuspend and not canRenew and not canTerminate}">
                <span class="ct-hint">Aucune action possible : ce contrat est résilié.</span>
            </c:if>
        </div>
    </section>
</c:if>

<div class="ct-grid-2">
    <%-- ---------- Informations ---------- --%>
    <section class="ct-panel">
        <h2>Informations</h2>
        <dl class="ct-dl">
            <dt>Client</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty client}"><c:out value="${client.fullName}"/><br><small><c:out value="${client.email}"/></small></c:when>
                    <c:otherwise>Client n°${contract.clientId}</c:otherwise>
                </c:choose>
            </dd>
            <dt>Type d'assurance</dt><dd><c:out value="${contract.insuranceType.name}"/></dd>
            <dt>Début</dt><dd>${contract.startDate}</dd>
            <dt>Fin</dt><dd>${contract.endDate}</dd>
            <dt>Créé le</dt><dd>${contract.createdAt.toLocalDate()}</dd>
            <dt>Agent responsable</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty agent}"><c:out value="${agent.fullName}"/></c:when>
                    <c:otherwise><em>Non assigné</em></c:otherwise>
                </c:choose>
            </dd>
        </dl>

        <c:if test="${not readOnly}">
            <form method="post" action="${action}" style="margin-top:18px">
                <input type="hidden" name="id" value="${contract.id}"><input type="hidden" name="op" value="assign">
                <div class="ct-form-row" style="align-items:end">
                    <div class="ct-field">
                        <label for="agentId">Assigner un agent</label>
                        <select id="agentId" name="agentId" required>
                            <c:forEach var="a" items="${agents}">
                                <option value="${a.id}" ${a.id == contract.agentId ? 'selected' : ''}><c:out value="${a.fullName}"/></option>
                            </c:forEach>
                        </select>
                    </div>
                    <div><button class="ct-btn ct-btn-light">Assigner</button></div>
                </div>
            </form>
        </c:if>
    </section>

    <%-- ---------- Garanties + calcul de la prime ---------- --%>
    <section class="ct-panel">
        <h2>Garanties souscrites</h2>
        <table class="ct-table">
            <thead><tr><th>Garantie</th><th class="ct-num">Supplément</th></tr></thead>
            <tbody>
            <tr>
                <td><em>Prix de base (<c:out value="${contract.insuranceType.name}"/>)</em></td>
                <td class="ct-num">${contract.insuranceType.basePrice} €</td>
            </tr>
            <c:forEach var="cc" items="${contract.coverages}">
                <tr>
                    <td><c:out value="${cc.coverage.name}"/></td>
                    <td class="ct-num">${cc.appliedPremium.signum() == 0 ? 'incluse' : '+ '.concat(cc.appliedPremium).concat(' €')}</td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <div class="ct-premium">
            <div>Prime annuelle</div>
            <div class="ct-amount">${contract.premiumAmount} €</div>
        </div>
    </section>
</div>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
