<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Créer un compte – Malin’Assur</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
</head>
<body>
  <main class="center-page">
    <section class="card">
      <img class="logo" src="${pageContext.request.contextPath}/img/logo.png" alt="Malin’Assur">

      <h1>Créer un compte</h1>
      <p class="subtitle">Rejoignez votre espace client Malin’Assur</p>

      <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
      </c:if>

      <form id="registerForm" action="${pageContext.request.contextPath}/register" method="post" novalidate>
        <div class="field">
          <label for="firstName">Prénom</label>
          <input type="text" id="firstName" name="firstName" maxlength="100"
                 autocomplete="given-name" required
                 value="<c:out value='${firstName}'/>">
          <div class="error-text">Veuillez saisir votre prénom.</div>
        </div>

        <div class="field">
          <label for="lastName">Nom</label>
          <input type="text" id="lastName" name="lastName" maxlength="100"
                 autocomplete="family-name" required
                 value="<c:out value='${lastName}'/>">
          <div class="error-text">Veuillez saisir votre nom.</div>
        </div>

        <div class="field">
          <label for="email">Adresse e-mail</label>
          <input type="email" id="email" name="email" maxlength="255"
                 placeholder="vous@exemple.fr" autocomplete="email" required
                 value="<c:out value='${email}'/>">
          <div class="error-text">Veuillez saisir une adresse e-mail valide.</div>
        </div>

        <div class="field">
          <label for="password">Mot de passe</label>
          <input type="password" id="password" name="password" maxlength="72"
                 autocomplete="new-password" required>
          <div class="error-text">8 caractères minimum, avec au moins une lettre et un chiffre.</div>
        </div>

        <div class="field">
          <label for="confirmPassword">Confirmer le mot de passe</label>
          <input type="password" id="confirmPassword" name="confirmPassword" maxlength="72"
                 autocomplete="new-password" required>
          <div class="error-text">Les deux mots de passe doivent être identiques.</div>
        </div>

        <p class="field-hint">8 caractères minimum, dont au moins une lettre et un chiffre.</p>

        <button type="submit" class="btn">Créer mon compte</button>
      </form>

      <div class="divider">◆</div>
      <p class="card-footer">Déjà un compte ?
        <a href="${pageContext.request.contextPath}/login">Se connecter</a>
      </p>
    </section>
  </main>

  <script src="${pageContext.request.contextPath}/js/register.js"></script>
</body>
</html>
