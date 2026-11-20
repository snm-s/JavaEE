<%-- Espace ADMIN : tous les contrats, en lecture seule  (servlet : AdminContractServlet, GET /admin/contracts) --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/contracts/top.jsp" %>

<div class="ct-page-head">
    <div>
        <h1>Ensemble des contrats</h1>
        <p class="ct-subtitle">Vue administrateur, en lecture seule.</p>
    </div>
</div>
<%@ include file="/WEB-INF/views/includes/contracts/subnav.jsp" %>
<%@ include file="/WEB-INF/views/includes/contracts/contract-table.jsp" %>

<%@ include file="/WEB-INF/views/includes/contracts/bottom.jsp" %>
