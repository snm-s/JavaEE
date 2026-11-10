<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
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

      <%-- Message d'erreur renvoyé par la servlet : request.setAttribute("error", "...") --%>
      <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
      <% } %>
      <%-- Message de succès (ex : après inscription) : request.setAttribute("success", "...") --%>
      <% if (request.getAttribute("success") != null) { %>
        <div class="alert alert-success"><%= request.getAttribute("success") %></div>
      <% } %>

      <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post" novalidate>
        <div class="field">
          <label for="email">Adresse e-mail</label>
          <input type="email" id="email" name="email" placeholder="vous@exemple.fr" autocomplete="email" required>
          <div class="error-text">Veuillez saisir une adresse e-mail valide.</div>
        </div>

        <div class="field">
          <label for="password">Mot de passe</label>
          <div class="input-wrap">
            <input type="password" id="password" name="password" placeholder="••••••••" autocomplete="current-password" required>
            <button type="button" class="toggle-pwd" id="togglePwd">Afficher</button>
          </div>
          <div class="error-text">Veuillez saisir votre mot de passe.</div>
        </div>

        <div class="row-between">
          <label class="remember"><input type="checkbox" name="remember"> Se souvenir de moi</label>
          <a href="${pageContext.request.contextPath}/forgot-password.jsp">Mot de passe oublié ?</a>
        </div>

        <button type="submit" class="btn">Se connecter</button>
      </form>

      <div class="divider">◆</div>
      <p class="card-footer">Pas encore de compte ? <a href="${pageContext.request.contextPath}/register.jsp">Créer un compte</a></p>
    </section>
  </main>

  <script src="${pageContext.request.contextPath}/js/login.js"></script>
</body>
</html>