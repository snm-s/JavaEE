<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Gestion des utilisateurs - Malin'Assur</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/contracts.css">
    <style>
        .admin-btn {
            display: inline-block;
            width: auto;
            padding: 9px 16px;
            font-size: 0.85rem;
        }
        .admin-btn-secondary {
            background: white;
            color: var(--bordeaux);
            border: 1px solid rgba(107, 30, 46, 0.35);
        }
        .admin-btn-secondary:hover {
            background: #f9e9ed;
            color: var(--bordeaux-dark);
            box-shadow: none;
        }
        .admin-table-wrap {
            background: white;
            border-radius: var(--radius);
            border: 1px solid #eee5dc;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
            overflow-x: auto;
        }
        .admin-table {
            width: 100%;
            border-collapse: collapse;
        }
        .admin-table th, .admin-table td {
            padding: 14px 16px;
            text-align: left;
            border-bottom: 1px solid #f0e8df;
        }
        .admin-table th {
            background: #fbf9f4;
            color: var(--sage-dark);
            font-size: 0.78rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        .admin-input, .admin-select {
            padding: 10px 14px;
            border: 1px solid var(--sage-light);
            border-radius: 10px;
            font: inherit;
            background: white;
            color: var(--text);
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="app-layout">
    <jsp:include page="/WEB-INF/views/includes/admin/sidebar.jsp">
        <jsp:param name="activePage" value="${roleFilter == 'CLIENT' ? 'clients' : 'users'}"/>
    </jsp:include>

    <main class="app-main">
        <div class="contracts-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px;">
            <div>
                <h1>${roleFilter == 'CLIENT' ? 'Gestion des clients' : 'Gestion des utilisateurs'}</h1>
                <p>Consultez, créez, modifiez ou désactivez les comptes de la plateforme.</p>
            </div>
            <a href="${pageContext.request.contextPath}/admin/users?action=new&role=${not empty roleFilter ? roleFilter : 'CLIENT'}"
               class="btn admin-btn">+ Nouvel utilisateur</a>
        </div>

        <c:if test="${not empty sessionScope.flashSuccess}">
            <div class="alert alert-success">
                <c:out value="${sessionScope.flashSuccess}"/>
            </div>
            <c:remove var="flashSuccess" scope="session"/>
        </c:if>

        <c:if test="${not empty sessionScope.flashError}">
            <div class="alert alert-error">
                <c:out value="${sessionScope.flashError}"/>
            </div>
            <c:remove var="flashError" scope="session"/>
        </c:if>

        <!-- Barre de recherche et filtre -->
        <form method="get" action="${pageContext.request.contextPath}/admin/users"
              style="display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 24px;">
            <input type="text" name="q" value="<c:out value='${keyword}'/>" class="admin-input"
                   placeholder="Rechercher par nom, prénom ou e-mail..." style="flex: 1; min-width: 240px;">
            <select name="role" class="admin-select">
                <option value="">Tous les rôles</option>
                <c:forEach var="r" items="${roles}">
                    <option value="${r}" ${roleFilter == r.name() ? 'selected' : ''}>${r}</option>
                </c:forEach>
            </select>
            <button type="submit" class="btn admin-btn">Filtrer</button>
            <a href="${pageContext.request.contextPath}/admin/users" class="btn admin-btn admin-btn-secondary">Réinitialiser</a>
        </form>

        <!-- Tableau des utilisateurs -->
        <div class="admin-table-wrap">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Nom complet</th>
                    <th>E-mail</th>
                    <th>Rôle</th>
                    <th>Statut</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty users}">
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 32px; color: var(--muted);">
                                Aucun utilisateur trouvé.
                            </td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="u" items="${users}">
                            <tr>
                                <td>#${u.id}</td>
                                <td><strong><c:out value="${u.fullName}"/></strong></td>
                                <td><c:out value="${u.email}"/></td>
                                <td>${u.role}</td>
                                <td>
                                    <span class="contract-status ${u.active ? 'status-ACTIF' : 'status-RESILIE'}">
                                        ${u.active ? 'ACTIF' : 'DÉSACTIVÉ'}
                                    </span>
                                </td>
                                <td>
                                    <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                                        <a href="${pageContext.request.contextPath}/admin/users?action=edit&id=${u.id}"
                                           class="btn admin-btn admin-btn-secondary">Modifier</a>

                                        <form method="post" action="${pageContext.request.contextPath}/admin/users" style="margin: 0;">
                                            <input type="hidden" name="action" value="toggleStatus">
                                            <input type="hidden" name="id" value="${u.id}">
                                            <button type="submit" class="btn admin-btn admin-btn-secondary">
                                                ${u.active ? 'Désactiver' : 'Activer'}
                                            </button>
                                        </form>

                                        <form method="post" action="${pageContext.request.contextPath}/admin/users" style="margin: 0;"
                                              onsubmit="return confirm('Confirmer la suppression définitive de cet utilisateur ?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${u.id}">
                                            <button type="submit" class="btn admin-btn" style="background: var(--error);">
                                                Supprimer
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

</body>
</html>