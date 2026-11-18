<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .forms-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0 28px; }
    .mini-card { background: #fff; padding: 20px; border-radius: var(--radius); border: 1px solid var(--sage-light); }
    .mini-card input, .mini-card select { width: 100%; padding: 9px 12px; margin-bottom: 10px; border-radius: 8px; border: 1px solid var(--sage-light); font: inherit; }
    .data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: var(--radius); overflow: hidden; }
    .data-table th, .data-table td { padding: 13px 15px; text-align: left; border-bottom: 1px solid var(--sage-light); font-size: .9rem; }
    .data-table th { background: rgba(107, 30, 46, .08); color: var(--bordeaux); font-size: .78rem; text-transform: uppercase; }
    .badge { padding: 4px 10px; border-radius: 20px; font-size: .76rem; font-weight: 600; }
    .badge-EN_ATTENTE { background: #fef3c7; color: #92400e; }
    .badge-EFFECTUE   { background: #dcfce7; color: #166534; }
    .badge-ECHOUE     { background: #fee2e2; color: #991b1b; }
</style>

<div class="app-layout">
    <%@ include file="../includes/agent/sidebar.jsp" %>

    <main class="app-main">
        <h1>Suivi des paiements & échéances</h1>
        <p style="color:var(--muted); margin:4px 0 16px;">Générez des échéances, enregistrez un règlement direct ou gérez les incidents de paiement.</p>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success">${sessionScope.success}</div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.error}">
            <div class="alert alert-error">${sessionScope.error}</div>
            <c:remove var="error" scope="session"/>
        </c:if>

        <div class="forms-grid">
            <div class="mini-card">
                <h3 style="margin-bottom:12px;">Planifier une échéance</h3>
                <form action="${pageContext.request.contextPath}/agent/payments" method="post">
                    <input type="hidden" name="action" value="createSchedule">
                    <select name="contractId" required>
                        <option value="">-- Contrat --</option>
                        <c:forEach var="ctr" items="${contracts}">
                            <option value="${ctr.id}">${ctr.contractNumber} (Prime : ${ctr.premiumAmount} €)</option>
                        </c:forEach>
                    </select>
                    <input type="number" step="0.01" min="1" name="amount" placeholder="Montant (€)" required>
                    <input type="date" name="dueDate" required>
                    <button type="submit" class="btn" style="background:var(--sage-dark); padding:10px;">Créer l'échéance</button>
                </form>
            </div>

            <div class="mini-card">
                <h3 style="margin-bottom:12px;">Enregistrer un paiement direct</h3>
                <form action="${pageContext.request.contextPath}/agent/payments" method="post">
                    <input type="hidden" name="action" value="recordDirect">
                    <select name="contractId" required>
                        <option value="">-- Contrat --</option>
                        <c:forEach var="ctr" items="${contracts}">
                            <option value="${ctr.id}">${ctr.contractNumber}</option>
                        </c:forEach>
                    </select>
                    <input type="number" step="0.01" min="1" name="amount" placeholder="Montant encaissé (€)" required>
                    <select name="paymentMethod" required>
                        <c:forEach var="m" items="${paymentMethods}">
                            <option value="${m}">${m}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn" style="padding:10px;">Encaisser immédiatement</button>
                </form>
            </div>
        </div>

        <table class="data-table">
            <thead>
                <tr>
                    <th>Référence</th>
                    <th>Contrat</th>
                    <th>Montant</th>
                    <th>Échéance</th>
                    <th>Moyen</th>
                    <th>Statut</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${payments}">
                    <tr>
                        <td><strong>${p.transactionReference}</strong></td>
                        <td>${p.contract.contractNumber}</td>
                        <td><strong>${p.amount} €</strong></td>
                        <td>${p.dueDate}</td>
                        <td>${not empty p.paymentMethod ? p.paymentMethod : '—'}</td>
                        <td><span class="badge badge-${p.status}">${p.status}</span></td>
                        <td>
                            <c:if test="${p.status != 'EFFECTUE'}">
                                <form action="${pageContext.request.contextPath}/agent/payments" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="validate">
                                    <input type="hidden" name="paymentId" value="${p.id}">
                                    <input type="hidden" name="paymentMethod" value="VIREMENT">
                                    <button type="submit" style="background:var(--sage-dark); color:#fff; border:0; padding:5px 10px; border-radius:6px; cursor:pointer;">Valider</button>
                                </form>
                                <c:if test="${p.status == 'EN_ATTENTE'}">
                                    <form action="${pageContext.request.contextPath}/agent/payments" method="post" style="display:inline;">
                                        <input type="hidden" name="action" value="fail">
                                        <input type="hidden" name="paymentId" value="${p.id}">
                                        <button type="submit" style="background:var(--error); color:#fff; border:0; padding:5px 10px; border-radius:6px; cursor:pointer;">Échec</button>
                                    </form>
                                </c:if>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>