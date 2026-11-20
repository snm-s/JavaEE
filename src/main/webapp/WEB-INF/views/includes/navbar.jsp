
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<header class="app-navbar">
    <div class="navbar-brand">
        <span class="brand-symbol">M</span>
        <span class="brand-name">Malin'Assur</span>
    </div>

    <div class="navbar-right">
        <c:if test="${not empty sessionScope.user}">
            <span class="navbar-user">
                <c:out value="${sessionScope.userName}"/>
                <c:if test="${not empty sessionScope.roleLabel}">
                    · <c:out value="${sessionScope.roleLabel}"/>
                </c:if>
            </span>
            <a class="navbar-logout"
               href="${pageContext.request.contextPath}/logout">Déconnexion</a>
        </c:if>
        <c:if test="${empty sessionScope.user}">
            <a class="navbar-logout"
               href="${pageContext.request.contextPath}/login">Se connecter</a>
        </c:if>
    </div>
</header>
