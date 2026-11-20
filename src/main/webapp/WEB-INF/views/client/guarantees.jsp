<%-- Espace CLIENT : mes garanties, regroupées par contrat actif  (servlet : ClientContractServlet, GET /client/guarantees) --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="ct-page-head">
    <div>
        <h1>Mes garanties</h1>
        <p class="ct-subtitle">Ce qui est couvert par vos contrats actifs.</p>
    </div>
</div>

<c:forEach var="ct" items="${contracts}">
    <section class="ct-panel">
        <h2><c:out value="${ct.insuranceType.name}"/> <small class="ct-hint">· ${ct.contractNumber}</small></h2>
        <table class="ct-table">
            <thead><tr><th>Garantie</th><th>Ce qui est couvert</th></tr></thead>
            <tbody>
            <c:forEach var="cc" items="${ct.coverages}">
                <tr>
                    <td><strong><c:out value="${cc.coverage.name}"/></strong></td>
                    <td><c:out value="${cc.coverage.description}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </section>
</c:forEach>
<c:if test="${empty contracts}">
    <section class="ct-panel ct-empty">Aucun contrat actif pour le moment.</section>
</c:if>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
