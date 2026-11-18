<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>${not empty targetUser ? 'Modifier' : 'Créer'} un utilisateur - Malin'Assur</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/contracts.css">
    <style>
        .form-panel {
            max-width: 580px;
            background: white;
            padding: 32px;
            border-radius: var(--radius);
            border: 1px solid #eee5dc;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }
        .field select {
            width: 100%;
            padding: 12px 14px;
            font: inherit;
            color: var(--text);
            background: rgba(255, 255, 255, .85);
            border: 1px solid var(--sage-light);
            border-radius: 10px;
            outline: none;
        }
        .admin-btn {
            display: inline-block;
            width: auto;
            padding: 11px 20px;
        }
        .admin-btn-secondary {
            background: white;
            color: var(--bordeaux);
            border: 1px solid rgba(107, 30, 46, 0.35);
        }
    </style>
</head>
<body>

<jsp:include page="/WEB-INF/views/includes/navbar.jsp"/>

<div class="app-layout">
    <jsp:include page="/WEB-INF/views/includes/admin/sidebar.jsp"/>

    <main class="app-main">
        <div class="contracts-header">
            <h1>${not empty targetUser ? 'Modifier l\'utilisateur' : 'Nouvel utilisateur'}</h1>
            <p>Renseignez les informations du compte et son rôle sur la plateforme.</p>
        </div>

        <div class="form-panel">
            <c:if test="${not empty error}">
                <div class="alert alert-error">
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form method="post" action="${pageContext.request.contextPath}/admin/users">
                <input type="hidden" name="action" value="${not empty targetUser ? 'update' : 'create'}">
                <c:if test="${not empty targetUser}">
                    <input type="hidden" name="id" value="${targetUser.id}">
                </c:if>

                <div class="field">
                    <label for="firstName">Prénom *</label>
                    <input type="text" id="firstName" name="firstName" required
                           value="<c:out value='${not empty param.firstName ? param.firstName : targetUser.firstName}'/>">
                </div>

                <div class="field">
                    <label for="lastName">Nom *</label>
                    <input type="text" id="lastName" name="lastName" required
                           value="<c:out value='${not empty param.lastName ? param.lastName : targetUser.lastName}'/>">
                </div>

                <div class="field">
                    <label for="email">Adresse e-mail *</label>
                    <input type="email" id="email" name="email" required
                           value="<c:out value='${not empty param.email ? param.email : targetUser.email}'/>">
                </div>

                <div class="field">
                    <label for="role">Rôle *</label>
                    <c:set var="selectedRole" value="${not empty param.role ? param.role : (not empty targetUser ? targetUser.role.name() : defaultRole)}"/>
                    <select id="role" name="role">
                        <c:forEach var="r" items="${roles}">
                            <option value="${r}" ${selectedRole == r.name() ? 'selected' : ''}>${r}</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="field">
                    <label for="password">
                        Mot de passe ${not empty targetUser ? '(laisser vide pour conserver l\'actuel)' : '* (6 caractères min.)'}
                    </label>
                    <input type="password" id="password" name="password" ${empty targetUser ? 'required' : ''}>
                </div>

                <div class="field">
                    <label class="remember" style="text-transform: none; letter-spacing: normal; font-size: 0.9rem;">
                        <input type="checkbox" name="active"
                               ${(empty targetUser and empty param.action) or (not empty targetUser and targetUser.active) or (not empty param.active) ? 'checked' : ''}>
                        Compte actif (autorisé à se connecter)
                    </label>
                </div>

                <div style="display: flex; gap: 12px; margin-top: 24px;">
                    <button type="submit" class="btn admin-btn">Enregistrer</button>
                    <a href="${pageContext.request.contextPath}/admin/users" class="btn admin-btn admin-btn-secondary">Annuler</a>
                </div>
            </form>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/includes/footer.jsp"/>

</body>
</html>