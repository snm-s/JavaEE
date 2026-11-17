
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Malin'Assur — Votre assurance, en toute confiance</title>

    <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;500;600;700&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="home-page">

    <header class="home-header">
        <a class="home-brand" href="${pageContext.request.contextPath}/">
            <span class="home-brand-icon">M</span>
            <span>Malin'Assur</span>
        </a>

        <nav class="home-nav" aria-label="Navigation principale">
            <a href="#offres">Nos assurances</a>
            <a href="#avantages">Pourquoi nous ?</a>
            <a class="home-nav-login" href="${pageContext.request.contextPath}/login">
                Se connecter
            </a>
        </nav>
    </header>

    <main>
        <section class="home-hero">
            <div class="home-hero-content">
                <span class="home-eyebrow">L'ASSURANCE REPENSÉE POUR VOUS</span>

                <h1>Protégez l'essentiel.<br>
                    <em>Vivez l'esprit libre.</em>
                </h1>

                <p>
                    Une assurance plus simple, plus claire et plus proche de vous.
                    Découvrez une nouvelle façon de protéger ce qui compte vraiment.
                </p>

                <div class="home-actions">
                    <a class="home-primary-btn" href="#offres">
                        Découvrir nos offres <span aria-hidden="true">↗</span>
                    </a>
                    <a class="home-secondary-btn"
                       href="${pageContext.request.contextPath}/login">
                        Mon espace client
                    </a>
                </div>
            </div>

            <div class="home-hero-signature">
                <span class="home-signature-line"></span>
                Votre assurance, en toute confiance.
            </div>
        </section>

        <section id="offres" class="home-section">
            <div class="home-section-heading">
                <span class="home-eyebrow">NOS SOLUTIONS</span>
                <h2>À chaque étape de votre vie.</h2>
                <p>Des protections pensées pour vos besoins du quotidien.</p>
            </div>

            <div class="home-offers">
                <article class="home-offer">
                    <span class="home-offer-number">01</span>
                    <h3>Automobile</h3>
                    <p>Pour prendre la route avec sérénité.</p>
                </article>
                <article class="home-offer">
                    <span class="home-offer-number">02</span>
                    <h3>Habitation</h3>
                    <p>Pour protéger votre chez-vous.</p>
                </article>
                <article class="home-offer">
                    <span class="home-offer-number">03</span>
                    <h3>Protection au quotidien</h3>
                    <p>Pour accompagner les moments qui comptent.</p>
                </article>
            </div>
        </section>

        <section id="avantages" class="home-bottom">
            <span class="home-eyebrow">L'ESPRIT MALIN'ASSUR</span>
            <h2>Moins de complications.<br>Plus de tranquillité.</h2>
            <p>Un espace pensé pour retrouver et suivre vos assurances simplement.</p>
        </section>
    </main>

    <footer class="home-footer">
        <span>© Malin'Assur</span>
        <span>Votre assurance, en toute confiance.</span>
    </footer>

</body>
</html>
