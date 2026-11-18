<%-- =====================================================================
     Espace AGENT : création OU modification d'un contrat
     Servlet : AgentContractServlet  (GET /agent/contracts/new  |  GET /agent/contracts/edit?id=)
     - création : on choisit client, type, garanties optionnelles, date, durée
     - modification : type et client figés, on change dates + garanties
     La prime est calculée en direct (JavaScript) puis RECALCULÉE côté serveur à l'enregistrement.
     ===================================================================== --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="editMode" value="${not empty contract}"/>
<%-- garanties recochées si le formulaire est réaffiché après une erreur --%>
<c:set var="posted" value=",${fn:join(paramValues.coverageIds, ',')},"/>

<div class="ct-page-head">
    <div>
        <h1>${editMode ? 'Modifier le contrat' : 'Nouveau contrat'}</h1>
        <p class="ct-subtitle">
            <c:choose>
                <c:when test="${editMode}">Contrat <strong>${contract.contractNumber}</strong> · <c:out value="${contract.insuranceType.name}"/></c:when>
                <c:otherwise>Le contrat est créé « en attente » : il faudra ensuite l'activer.</c:otherwise>
            </c:choose>
        </p>
    </div>
    <a href="${ctx}/agent/contracts${editMode ? '/view?id='.concat(contract.id) : ''}" class="ct-btn ct-btn-light">← Retour</a>
</div>
<%@ include file="/WEB-INF/views/includes/contracts/subnav.jsp" %>

<form method="post" action="${ctx}/agent/contracts/save" id="contractForm">
    <c:if test="${editMode}"><input type="hidden" name="id" value="${contract.id}"></c:if>

    <%-- ---------- 1. Client et type ---------- --%>
    <section class="ct-panel">
        <h2>1. Client et produit</h2>
        <div class="ct-form-row">
            <div class="ct-field">
                <label for="clientId">Client *</label>
                <select id="clientId" name="clientId" ${editMode ? 'disabled' : 'required'}>
                    <option value="">— Choisir un client —</option>
                    <c:forEach var="cl" items="${clients}">
                        <c:set var="isSel" value="${editMode ? cl.id == contract.clientId : cl.id == param.clientId}"/>
                        <option value="${cl.id}" ${isSel ? 'selected' : ''}><c:out value="${cl.fullName}"/> (<c:out value="${cl.email}"/>)</option>
                    </c:forEach>
                </select>
            </div>
            <div class="ct-field">
                <label for="typeId">Type d'assurance *</label>
                <select id="typeId" name="typeId" ${editMode ? 'disabled' : 'required'}>
                    <option value="" data-base="0">— Choisir un type —</option>
                    <c:forEach var="t" items="${types}">
                        <c:set var="isSel" value="${editMode ? t.id == contract.insuranceType.id : t.id == param.typeId}"/>
                        <option value="${t.id}" data-base="${t.basePrice}" ${isSel ? 'selected' : ''}>
                            <c:out value="${t.name}"/> — ${t.basePrice} € de base
                        </option>
                    </c:forEach>
                </select>
                <c:if test="${editMode}"><div class="ct-hint">Le client et le type ne se modifient pas : créez un nouveau contrat si besoin.</div></c:if>
            </div>
        </div>
    </section>

    <%-- ---------- 2. Garanties (un bloc par type, seul celui du type choisi est visible) ---------- --%>
    <section class="ct-panel">
        <h2>2. Garanties</h2>
        <p class="ct-hint" id="noTypeHint">Choisissez d'abord un type d'assurance.</p>
        <c:forEach var="t" items="${types}">
            <div class="ct-coverages" data-type="${t.id}" hidden>
                <c:forEach var="o" items="${offers[t.id]}">
                    <c:set var="cid" value="${o.coverage.id}"/>
                    <c:set var="checked" value="${o['default']
                        or (editMode and selectedIds.contains(cid))
                        or fn:contains(posted, ','.concat(cid).concat(','))}"/>
                    <label class="ct-cov">
                        <input type="checkbox" name="coverageIds" value="${cid}"
                               data-premium="${o.additionalPremium}"
                               ${checked ? 'checked' : ''} ${o['default'] ? 'disabled' : ''}>
                        <span>
                            <strong><c:out value="${o.coverage.name}"/></strong>
                            <c:choose>
                                <c:when test="${o['default']}"><span class="ct-badge ct-tag-default">Incluse</span></c:when>
                                <c:otherwise><span class="ct-badge ct-tag-option">+ ${o.additionalPremium} €</span></c:otherwise>
                            </c:choose>
                            <br><small><c:out value="${o.coverage.description}"/></small>
                        </span>
                    </label>
                </c:forEach>
                <c:if test="${empty offers[t.id]}"><p class="ct-hint">Aucune garantie au catalogue de ce type.</p></c:if>
            </div>
        </c:forEach>
        <p class="ct-hint">Les garanties « incluses » sont toujours ajoutées au contrat.</p>
    </section>

    <%-- ---------- 3. Dates ---------- --%>
    <section class="ct-panel">
        <h2>3. Période</h2>
        <div class="ct-form-row">
            <div class="ct-field">
                <label for="startDate">Date de début *</label>
                <input type="date" id="startDate" name="startDate" required
                       value="${editMode ? contract.startDate : (empty param.startDate ? today : param.startDate)}">
            </div>
            <c:choose>
                <c:when test="${editMode}">
                    <div class="ct-field">
                        <label for="endDate">Date de fin *</label>
                        <input type="date" id="endDate" name="endDate" required value="${contract.endDate}">
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="ct-field">
                        <label for="durationMonths">Durée *</label>
                        <select id="durationMonths" name="durationMonths" required>
                            <c:forEach var="m" items="6,12,24,36">
                                <option value="${m}" ${m == (empty param.durationMonths ? '12' : param.durationMonths) ? 'selected' : ''}>${m} mois</option>
                            </c:forEach>
                        </select>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <%-- ---------- Prime calculée en direct ---------- --%>
    <div class="ct-premium">
        <div>
            <div>Prime annuelle estimée</div>
            <small id="premiumDetail">prix de base + garanties choisies</small>
        </div>
        <div class="ct-amount"><span id="premium">0.00</span> €</div>
    </div>

    <div class="ct-actions">
        <button type="submit" class="ct-btn">${editMode ? 'Enregistrer les modifications' : 'Créer le contrat'}</button>
        <a href="${ctx}/agent/contracts" class="ct-btn ct-btn-light">Annuler</a>
    </div>
</form>

<script>
    // Affiche les garanties du type choisi et recalcule la prime (aperçu uniquement :
    // la vraie prime est recalculée par ContractService côté serveur).
    (function () {
        const typeSelect = document.getElementById('typeId');
        const blocks = document.querySelectorAll('.ct-coverages[data-type]');
        const hint = document.getElementById('noTypeHint');

        function refresh() {
            const typeId = typeSelect.value;
            let total = parseFloat(typeSelect.selectedOptions[0]?.dataset.base || '0');
            let nb = 0;
            blocks.forEach(b => {
                const visible = b.dataset.type === typeId;
                b.hidden = !visible;
                // les cases des autres types ne doivent pas être envoyées
                b.querySelectorAll('input').forEach(i => {
                    if (!i.hasAttribute('data-locked')) i.dataset.locked = i.disabled ? '1' : '0';
                    i.disabled = !visible || i.dataset.locked === '1';
                    if (visible && i.checked) { total += parseFloat(i.dataset.premium || '0'); nb++; }
                });
            });
            hint.hidden = !!typeId;
            document.getElementById('premium').textContent = total.toFixed(2);
            document.getElementById('premiumDetail').textContent =
                typeId ? nb + ' garantie(s) sélectionnée(s)' : 'choisissez un type';
        }
        typeSelect.addEventListener('change', refresh);
        document.querySelectorAll('.ct-coverages input').forEach(i => i.addEventListener('change', refresh));
        refresh();
    })();
</script>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
