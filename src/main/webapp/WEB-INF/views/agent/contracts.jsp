<%-- Espace AGENT : liste et recherche des contrats  (servlet : AgentContractServlet, GET /agent/contracts) --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>

<div class="ct-page-head">
    <div>
        <h1>Contrats</h1>
        <p class="ct-subtitle">Rechercher, consulter et gérer les contrats des clients.</p>
    </div>
    <a href="${pageContext.request.contextPath}/agent/contracts/new" class="ct-btn">+ Nouveau contrat</a>
</div>
<%@ include file="/WEB-INF/views/includes/contracts/subnav.jsp" %>
<%@ include file="/WEB-INF/views/includes/contracts/contract-table.jsp" %>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
