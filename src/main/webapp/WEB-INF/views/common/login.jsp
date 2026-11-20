<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Connexion – Malin’Assur</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
</head>
<body>
  <main class="center-page">
    <section class="card">
      <img class="logo" src="${pageContext.request.contextPath}/img/logo.png" alt="Malin’Assur">

      <h1>Bienvenue</h1>
      <p class="subtitle">Connectez-vous à votre espace personnel</p>

      <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
      </c:if>
      <c:if test="${not empty success}">
        <div class="alert alert-success"><c:out value="${success}"/></div>
      </c:if>

      <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post" novalidate>
        <input type="hidden" name="redirect" value="<c:out value='${param.redirect}'/>">

        <div class="field">
          <label for="email">Adresse e-mail</label>
          <input type="email" id="email" name="email" maxlength="255"
                 placeholder="vous@exemple.fr" autocomplete="email" required
                 value="<c:out value='${email}'/>">
          <div class="error-text">Veuillez saisir une adresse e-mail valide.</div>
        </div>

        <div class="field">
          <label for="password">Mot de passe</label>
          <div class="input-wrap">
            <input type="password" id="password" name="password" maxlength="72"
                   placeholder="••••••••" autocomplete="current-password" required>
            <button type="button" class="toggle-pwd" id="togglePwd">Afficher</button>
          </div>
          <div class="error-text">Veuillez saisir votre mot de passe.</div>
        </div>

        <div class="row-between">
          <label class="remember"><input type="checkbox" name="remember"> Se souvenir de moi</label>
        </div>

        <button type="submit" class="btn">Se connecter</button>
      </form>

      <div class="divider">◆</div>
      <p class="card-footer">Pas encore de compte ?
        <a href="${pageContext.request.contextPath}/register">Créer un compte</a>
      </p>
    </section>
  </main>

  <script src="${pageContext.request.contextPath}/js/login.js"></script>
</body>
</html>
