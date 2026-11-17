<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin: 20px 0; }
    .info-box { background: #fff; padding: 14px 16px; border-radius: 10px; border: 1px solid var(--sage-light); }
    .info-box span { display: block; font-size: .75rem; text-transform: uppercase; color: var(--sage-dark); margin-bottom: 4px; }
    .doc-list { list-style: none; padding: 0; margin: 12px 0 24px; }
    .doc-list li { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; border: 1px solid var(--sage-light); border-radius: 10px; margin-bottom: 8px; }
</style>

<div class="app-layout">
    <%@ include file="../includes/client/sidebar.jsp" %>

    <main class="app-main">
        <div class="card" style="max-width: 780px; margin: 0 auto;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h1 style="text-align:left;">Dossier ${claim.claimNumber}</h1>
                <span class="alert alert-success" style="margin:0; padding:6px 14px; font-weight:600;">${claim.status}</span>
            </div>
            <p class="subtitle" style="text-align:left;">Contrat rattaché : <strong>${claim.contract.contractNumber}</strong></p>

            <div class="info-grid">
                <div class="info-box"><span>Type de sinistre</span><strong>${claim.claimType}</strong></div>
                <div class="info-box"><span>Date de l'incident</span><strong>${claim.incidentDate}</strong></div>
                <div class="info-box"><span>Estimation déclarée</span><strong>${claim.estimatedAmount} €</strong></div>
                <div class="info-box">
                    <span>Indemnité accordée</span>
                    <strong>${not empty claim.indemnityAmount ? claim.indemnityAmount.toString().concat(' €') : 'En cours d\'évaluation'}</strong>
                </div>
            </div>

            <div class="info-box" style="margin-bottom:16px;">
                <span>Description de l'incident</span>
                <p style="margin:4px 0 0; line-height:1.5;">${claim.description}</p>
            </div>

            <c:if test="${not empty claim.agentComment}">
                <div class="alert alert-success">
                    <strong>Note de votre gestionnaire :</strong><br>${claim.agentComment}
                </div>
            </c:if>

            <h3 style="margin-top:20px;">Pièces justificatives</h3>
            <c:choose>
                <c:when test="${empty claim.supportingDocuments}">
                    <p style="color:var(--muted); font-size:.9rem;">Aucune pièce justificative jointe.</p>
                </c:when>
                <c:otherwise>
                    <ul class="doc-list">
                        <c:forEach var="doc" items="${claim.supportingDocuments}">
                            <li>
                                <span>📄 ${doc.fileName} <small style="color:var(--muted);">(${doc.mimeType})</small></span>
                                <a href="${pageContext.request.contextPath}/documents/download?id=${doc.id}" target="_blank" style="font-weight:600;">Télécharger / Voir</a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>

            <a href="${pageContext.request.contextPath}/client/claims" class="btn" style="text-align:center;">← Retour à mes sinistres</a>
        </div>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>