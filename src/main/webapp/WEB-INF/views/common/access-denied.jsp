<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Accès refusé – Malin’Assur</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
</head>
<body>
  <main class="center-page">
    <section class="card">
      <img class="logo" src="${pageContext.request.contextPath}/img/logo.png" alt="Malin’Assur">

      <h1>Accès refusé</h1>
      <p class="subtitle">
        Vous n’avez pas les droits nécessaires pour consulter cette page.
      </p>

      <a class="btn"
         href="${pageContext.request.contextPath}${empty homePath ? '/login' : homePath}">
        Retour à mon espace
      </a>

      <div class="divider">◆</div>
      <p class="card-footer">
        <a href="${pageContext.request.contextPath}/logout">Se déconnecter</a>
      </p>
    </section>
  </main>
</body>
</html>
