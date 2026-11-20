<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin: 20px 0; }
    .info-box { background: #fff; padding: 14px 16px; border-radius: 10px; border: 1px solid var(--sage-light); }
    .info-box span { display: block; font-size: .75rem; text-transform: uppercase; color: var(--sage-dark); margin-bottom: 4px; }
    .decision-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 24px; }
    .decision-box { background: #fff; padding: 18px; border-radius: 12px; border: 1px solid var(--sage-light); }
    .decision-box textarea, .decision-box input { width: 100%; padding: 10px; border-radius: 8px; border: 1px solid var(--sage-light); font: inherit; margin-bottom: 10px; }
</style>

<div class="app-layout">
    <%@ include file="../includes/agent/sidebar.jsp" %>

    <main class="app-main">
        <div class="card" style="max-width: 860px; margin: 0 auto;">
            <div style="display:flex; justify-content:space-between; align-items:center;">
                <h1 style="text-align:left;">Instruction : ${claim.claimNumber}</h1>
                <span class="alert alert-success" style="margin:0; padding:6px 14px; font-weight:600;">${claim.status}</span>
            </div>
            <p class="subtitle" style="text-align:left;">Contrat : <strong>${claim.contract.contractNumber}</strong> (Client ID #${claim.contract.clientId})</p>

            <c:if test="${not empty sessionScope.success}">
                <div class="alert alert-success">${sessionScope.success}</div>
                <c:remove var="success" scope="session"/>
            </c:if>
            <c:if test="${not empty sessionScope.error}">
                <div class="alert alert-error">${sessionScope.error}</div>
                <c:remove var="error" scope="session"/>
            </c:if>

            <div class="info-grid">
                <div class="info-box"><span>Nature</span><strong>${claim.claimType}</strong></div>
                <div class="info-box"><span>Date de l'incident</span><strong>${claim.incidentDate}</strong></div>
                <div class="info-box"><span>Estimation client</span><strong>${claim.estimatedAmount} €</strong></div>
                <div class="info-box"><span>Indemnité validée</span><strong>${not empty claim.indemnityAmount ? claim.indemnityAmount.toString().concat(' €') : 'Non définie'}</strong></div>
            </div>

            <div class="info-box" style="margin-bottom:16px;">
                <span>Description</span>
                <p style="margin:4px 0 0;">${claim.description}</p>
            </div>

            <h3>Pièces justificatives</h3>
            <c:choose>
                <c:when test="${empty claim.supportingDocuments}">
                    <p style="color:var(--muted); font-size:.9rem;">Aucune pièce jointe.</p>
                </c:when>
                <c:otherwise>
                    <ul style="list-style:none; padding:0;">
                        <c:forEach var="doc" items="${claim.supportingDocuments}">
                            <li style="background:#fff; padding:10px 14px; border:1px solid var(--sage-light); border-radius:8px; margin-bottom:8px; display:flex; justify-content:space-between;">
                                <span>📄 ${doc.fileName}</span>
                                <a href="${pageContext.request.contextPath}/documents/download?id=${doc.id}" target="_blank" style="font-weight:600;">Ouvrir le document</a>
                            </li>
                        </c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>

            <!-- Actions selon l'état du workflow -->
            <c:if test="${claim.status == 'DECLARE'}">
                <form action="${pageContext.request.contextPath}/agent/claims" method="post" style="margin-top:20px;">
                    <input type="hidden" name="claimId" value="${claim.id}">
                    <input type="hidden" name="action" value="review">
                    <button type="submit" class="btn" style="background:var(--sage-dark);">Prendre en charge le dossier (Passer EN_ANALYSE)</button>
                </form>
            </c:if>

            <c:if test="${claim.status == 'DECLARE' || claim.status == 'EN_ANALYSE'}">
                <div class="decision-grid">
                    <div class="decision-box">
                        <h3 style="color:var(--sage-dark); margin-bottom:10px;">Accepter & Indemniser</h3>
                        <form action="${pageContext.request.contextPath}/agent/claims" method="post">
                            <input type="hidden" name="claimId" value="${claim.id}">
                            <input type="hidden" name="action" value="accept">
                            <input type="number" step="0.01" min="1" name="indemnityAmount" value="${claim.estimatedAmount}" placeholder="Montant accordé (€)" required>
                            <textarea name="agentComment" rows="2" placeholder="Commentaire pour le client (optionnel)"></textarea>
                            <button type="submit" class="btn" style="background:var(--sage-dark);">Valider l'acceptation</button>
                        </form>
                    </div>

                    <div class="decision-box">
                        <h3 style="color:var(--error); margin-bottom:10px;">Refuser le sinistre</h3>
                        <form action="${pageContext.request.contextPath}/agent/claims" method="post">
                            <input type="hidden" name="claimId" value="${claim.id}">
                            <input type="hidden" name="action" value="reject">
                            <textarea name="agentComment" rows="4" placeholder="Motif obligatoire du refus..." required></textarea>
                            <button type="submit" class="btn">Confirmer le refus</button>
                        </form>
                    </div>
                </div>
            </c:if>

            <c:if test="${claim.status == 'ACCEPTE'}">
                <form action="${pageContext.request.contextPath}/agent/claims" method="post" style="margin-top:20px;">
                    <input type="hidden" name="claimId" value="${claim.id}">
                    <input type="hidden" name="action" value="indemnify">
                    <button type="submit" class="btn" style="background:var(--sage-dark);">Confirmer le virement de l'indemnité (Passer INDEMNISE)</button>
                </form>
            </c:if>

            <div style="margin-top:24px;">
                <a href="${pageContext.request.contextPath}/agent/claims">← Retour à la liste des sinistres</a>
            </div>
        </div>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>