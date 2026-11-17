<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .field select, .field textarea {
        width: 100%; padding: 12px 14px; font: inherit; color: var(--text);
        background: rgba(255, 255, 255, .95); border: 1px solid var(--sage-light);
        border-radius: 10px; outline: none;
    }
    .field select:focus, .field textarea:focus { border-color: var(--sage); box-shadow: 0 0 0 3px rgba(125, 149, 119, .22); }
    .form-actions { display: flex; gap: 12px; margin-top: 24px; }
    .btn-secondary { background: var(--sage-dark); text-align: center; }
</style>

<div class="app-layout">
    <%@ include file="../includes/client/sidebar.jsp" %>

    <main class="app-main">
        <div class="card" style="max-width: 680px; margin: 0 auto;">
            <h1 style="text-align:left;">Déclarer un sinistre</h1>
            <p class="subtitle" style="text-align:left;">Renseignez les circonstances de l'incident et joignez un justificatif.</p>

            <c:if test="${not empty error}">
                <div class="alert alert-error">${error}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/client/claims" method="post" enctype="multipart/form-data">
                <div class="field">
                    <label for="contractId">Contrat concerné *</label>
                    <select id="contractId" name="contractId" required>
                        <option value="">-- Sélectionnez un contrat actif --</option>
                        <c:forEach var="ctr" items="${contracts}">
                            <option value="${ctr.id}">${ctr.contractNumber} (${ctr.status} – du ${ctr.startDate} au ${ctr.endDate})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="field">
                    <label for="incidentDate">Date de l'incident *</label>
                    <input type="date" id="incidentDate" name="incidentDate" required>
                </div>

                <div class="field">
                    <label for="claimType">Nature du sinistre *</label>
                    <select id="claimType" name="claimType" required>
                        <option value="Accident matériel">Accident matériel</option>
                        <option value="Dégât des eaux">Dégât des eaux</option>
                        <option value="Vol / Vandalisme">Vol / Vandalisme</option>
                        <option value="Incendie">Incendie</option>
                        <option value="Bris de glace">Bris de glace</option>
                        <option value="Autre">Autre incident</option>
                    </select>
                </div>

                <div class="field">
                    <label for="estimatedAmount">Montant estimé des dommages (€) *</label>
                    <input type="number" step="0.01" min="1" id="estimatedAmount" name="estimatedAmount" placeholder="Ex : 450.00" required>
                </div>

                <div class="field">
                    <label for="description">Description détaillée des faits *</label>
                    <textarea id="description" name="description" rows="4" placeholder="Décrivez précisément les circonstances, lieux, tiers impliqués..." required></textarea>
                </div>

                <div class="field">
                    <label for="document">Pièce justificative (PDF, JPG, PNG – max 10 Mo)</label>
                    <input type="file" id="document" name="document" accept=".pdf,.jpg,.jpeg,.png,.webp">
                </div>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/client/claims" class="btn btn-secondary">Annuler</a>
                    <button type="submit" class="btn">Envoyer la déclaration</button>
                </div>
            </form>
        </div>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>