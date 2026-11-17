
<%@ page pageEncoding="UTF-8" %>
<%
    String currentPath = request.getRequestURI();
%>

<aside class="client-sidebar">
    <div class="sidebar-heading">
        <span class="sidebar-section-title">ESPACE CLIENT</span>
    </div>

    <nav class="sidebar-menu" aria-label="Navigation client">
        <a href="${pageContext.request.contextPath}/client/dashboard"
           class="sidebar-link <%= currentPath.endsWith("/client/dashboard") ? "active" : "" %>">
            Tableau de bord
        </a>

        <a href="${pageContext.request.contextPath}/client/contracts"
           class="sidebar-link <%= currentPath.endsWith("/client/contracts") ? "active" : "" %>">
            Mes contrats
        </a>

        <a href="${pageContext.request.contextPath}/client/guarantees"
           class="sidebar-link <%= currentPath.endsWith("/client/guarantees") ? "active" : "" %>">
            Mes garanties
        </a>

        <a href="${pageContext.request.contextPath}/client/payments"
           class="sidebar-link <%= currentPath.endsWith("/client/payments") ? "active" : "" %>">
            Mes paiements
        </a>

        <a href="${pageContext.request.contextPath}/client/claims"
           class="sidebar-link <%= currentPath.endsWith("/client/claims") ? "active" : "" %>">
            Mes sinistres
        </a>

        <a href="${pageContext.request.contextPath}/client/chatbot"
           class="sidebar-link <%= currentPath.endsWith("/client/chatbot") ? "active" : "" %>">
            Assistant IA
        </a>

        <a href="${pageContext.request.contextPath}/client/profile"
           class="sidebar-link <%= currentPath.endsWith("/client/profile") ? "active" : "" %>">
            Mon profil
        </a>
    </nav>

    <div class="sidebar-bottom">
        <a href="${pageContext.request.contextPath}/logout"
           class="sidebar-link">
            Déconnexion
        </a>
    </div>
</aside>
