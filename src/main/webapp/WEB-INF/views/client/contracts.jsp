<%-- Espace CLIENT : mes contrats  (servlet : ClientContractServlet, GET /client/contracts) --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="ct-page-head">
    <div>
        <h1>Mes contrats</h1>
        <p class="ct-subtitle">Retrouvez vos assurances, leur statut et leur prime annuelle.</p>
    </div>
</div>

<div class="ct-cards">
    <c:forEach var="ct" items="${contracts}">
        <article class="ct-card">
            <div class="ct-actions" style="justify-content:space-between">
                <span class="ct-meta">${ct.contractNumber}</span>
                <c:set var="st" value="${ct.status}"/><%@ include file="/WEB-INF/views/includes/contracts/status-badge.jsp" %>
            </div>
            <h3><c:out value="${ct.insuranceType.name}"/></h3>
            <div class="ct-meta">Du ${ct.startDate} au ${ct.endDate}</div>
            <div class="ct-meta">${ct.coverages.size()} garantie(s)</div>
            <div class="ct-price">${ct.premiumAmount} € <small style="font-size:.9rem">/ an</small></div>
            <a href="${ctx}/client/contracts/details?id=${ct.id}" class="ct-btn ct-btn-light">Voir le détail</a>
        </article>
    </c:forEach>
</div>
<c:if test="${empty contracts}">
    <section class="ct-panel ct-empty">Vous n'avez pas encore de contrat. Contactez votre agent pour souscrire.</section>
</c:if>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
