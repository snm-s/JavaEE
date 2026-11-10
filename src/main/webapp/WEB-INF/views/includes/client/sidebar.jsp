<%@ page pageEncoding="UTF-8" %>

<aside class="client-sidebar">
    <div class="sidebar-heading">
        <span class="sidebar-section-title">ESPACE CLIENT</span>
    </div>

    <nav class="sidebar-menu" aria-label="Navigation client">
        <a href="${pageContext.request.contextPath}/client/dashboard"
           class="sidebar-link">
            Tableau de bord
        </a>

        <a href="${pageContext.request.contextPath}/client/contracts"
           class="sidebar-link">
            Mes contrats
        </a>

        <a href="${pageContext.request.contextPath}/client/guarantees"
           class="sidebar-link">
            Mes garanties
        </a>

        <a href="${pageContext.request.contextPath}/client/payments"
           class="sidebar-link">
            Mes paiements
        </a>

        <a href="${pageContext.request.contextPath}/client/claims"
           class="sidebar-link">
            Mes sinistres
        </a>

        <a href="${pageContext.request.contextPath}/client/chatbot"
           class="sidebar-link">
            Assistant IA
        </a>

        <a href="${pageContext.request.contextPath}/client/profile"
           class="sidebar-link">
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
