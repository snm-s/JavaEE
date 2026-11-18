<%-- Espace CLIENT : détail d'un de mes contrats  (servlet : ClientContractServlet, GET /client/contracts/details?id=) --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="ct-page-head">
    <div>
        <h1><c:out value="${contract.insuranceType.name}"/></h1>
        <p class="ct-subtitle">Contrat ${contract.contractNumber} ·
            <c:set var="st" value="${contract.status}"/><%@ include file="/WEB-INF/views/includes/contracts/status-badge.jsp" %>
        </p>
    </div>
    <a href="${ctx}/client/contracts" class="ct-btn ct-btn-light">← Mes contrats</a>
</div>

<div class="ct-grid-2">
    <section class="ct-panel">
        <h2>Mon contrat</h2>
        <dl class="ct-dl">
            <dt>Numéro</dt><dd>${contract.contractNumber}</dd>
            <dt>Produit</dt><dd><c:out value="${contract.insuranceType.name}"/></dd>
            <dt>Début</dt><dd>${contract.startDate}</dd>
            <dt>Fin</dt><dd>${contract.endDate}</dd>
            <dt>Prime annuelle</dt><dd><strong>${contract.premiumAmount} €</strong></dd>
        </dl>
        <c:if test="${contract.status == 'ACTIF'}">
            <p class="ct-hint" style="margin-top:16px">Un problème ? Vous pouvez déclarer un sinistre depuis « Mes sinistres ».</p>
        </c:if>
    </section>

    <section class="ct-panel">
        <h2>Mes garanties</h2>
        <table class="ct-table">
            <thead><tr><th>Garantie</th><th>Ce qui est couvert</th></tr></thead>
            <tbody>
            <c:forEach var="cc" items="${contract.coverages}">
                <tr>
                    <td><strong><c:out value="${cc.coverage.name}"/></strong></td>
                    <td><c:out value="${cc.coverage.description}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </section>
</div>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
