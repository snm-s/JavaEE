<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Inscription – Malin’Assur</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
</head>
<body>
  <main class="center-page">
    <section class="card" style="max-width: 480px;">
      <img class="logo" src="${pageContext.request.contextPath}/img/logo.png" alt="Malin’Assur">

      <h1>Créer un compte</h1>
      <p class="subtitle">Rejoignez Malin’Assur et gérez vos assurances en ligne</p>

      <% if (request.getAttribute("error") != null) { %>
        <div class="alert alert-error"><%= request.getAttribute("error") %></div>
      <% } %>

      <form action="${pageContext.request.contextPath}/register" method="post">
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
          <div class="field">
            <label for="firstName">Prénom</label>
            <input type="text" id="firstName" name="firstName" value="${firstName}" placeholder="Claire" required>
          </div>
          <div class="field">
            <label for="lastName">Nom</label>
            <input type="text" id="lastName" name="lastName" value="${lastName}" placeholder="Dupont" required>
          </div>
        </div>

        <div class="field">
          <label for="email">Adresse e-mail</label>
          <input type="email" id="email" name="email" value="${email}" placeholder="vous@exemple.fr" autocomplete="email" required>
        </div>

        <div class="field">
          <label for="password">Mot de passe (min. 6 caractères)</label>
          <input type="password" id="password" name="password" placeholder="••••••••" autocomplete="new-password" required>
        </div>

        <div class="field">
          <label for="confirmPassword">Confirmer le mot de passe</label>
          <input type="password" id="confirmPassword" name="confirmPassword" placeholder="••••••••" autocomplete="new-password" required>
        </div>

        <button type="submit" class="btn">S'inscrire</button>
      </form>

      <div class="divider">◆</div>
      <p class="card-footer">Déjà inscrit ? <a href="${pageContext.request.contextPath}/login">Se connecter</a></p>
    </section>
  </main>
</body>
</html>