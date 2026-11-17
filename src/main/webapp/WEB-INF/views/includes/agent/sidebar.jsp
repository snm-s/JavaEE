<%@ page pageEncoding="UTF-8" %>
<aside class="client-sidebar">
    <div class="sidebar-heading">
        <span class="sidebar-section-title">ESPACE AGENT</span>
    </div>
    <nav class="sidebar-menu" aria-label="Navigation agent">
        <a href="${pageContext.request.contextPath}/agent/dashboard" class="sidebar-link">Tableau de bord</a>
        <a href="${pageContext.request.contextPath}/agent/contracts" class="sidebar-link">Contrats</a>
        <a href="${pageContext.request.contextPath}/agent/claims" class="sidebar-link">Sinistres</a>
        <a href="${pageContext.request.contextPath}/agent/payments" class="sidebar-link">Paiements</a>
        <a href="${pageContext.request.contextPath}/client/claims" class="sidebar-link" style="color:#e4c9ce; margin-top:12px;">⇄ Basculer Espace Client</a>
    </nav>
    <div class="sidebar-bottom">
        <a href="${pageContext.request.contextPath}/logout" class="sidebar-link">Déconnexion</a>
    </div>
</aside>