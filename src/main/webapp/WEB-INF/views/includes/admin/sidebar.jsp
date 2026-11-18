<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<aside class="client-sidebar">
    <div class="sidebar-heading">
        <span class="sidebar-section-title">ESPACE ADMINISTRATEUR</span>
    </div>

    <nav class="sidebar-menu">
        <a class="sidebar-link ${param.activePage == 'clients' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/admin/users?role=CLIENT">Clients</a>
        <a class="sidebar-link ${param.activePage == 'users' ? 'active' : ''}"
           href="${pageContext.request.contextPath}/admin/users">Tous les utilisateurs</a>
        <a class="sidebar-link"
           href="${pageContext.request.contextPath}/admin/contracts">Contrats</a>
        <a class="sidebar-link"
           href="${pageContext.request.contextPath}/admin/insurance-types">Types d'assurance</a>
        <a class="sidebar-link"
           href="${pageContext.request.contextPath}/admin/guarantees">Garanties</a>
    </nav>

    <div class="sidebar-bottom">
        <a class="sidebar-link" href="${pageContext.request.contextPath}/logout">Déconnexion</a>
    </div>
</aside>