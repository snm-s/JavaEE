<%-- =====================================================================
     Haut de page commun aux pages du module Contrats & Produits.
     Réutilise le header / navbar / sidebar de l'équipe UI.
     La servlet doit avoir fait : request.setAttribute("role", "agent" | "admin" | "client")
     ===================================================================== --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="/WEB-INF/views/includes/header.jsp" %>
<%@ include file="/WEB-INF/views/includes/navbar.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/contracts.css">

<div class="app-layout">
    <jsp:include page="/WEB-INF/views/includes/${role}/sidebar.jsp"/>

    <main class="app-main">
        <%-- Messages "flash" (après une redirection), affichés une seule fois --%>
        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="ct-alert ct-alert-success"><c:out value="${sessionScope.flashSuccess}"/></div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.flashError}">
            <div class="ct-alert ct-alert-error"><c:out value="${sessionScope.flashError}"/></div>
            <c:remove var="flashError" scope="session"/>
        </c:if>
        <%-- Erreur directe (formulaire réaffiché) --%>
        <c:if test="${not empty error}">
            <div class="ct-alert ct-alert-error"><c:out value="${error}"/></div>
        </c:if>
