<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: var(--radius); overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,.04); margin-top: 20px; }
    .data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid var(--sage-light); font-size: .92rem; }
    .data-table th { background: rgba(125, 149, 119, .15); color: var(--sage-dark); font-size: .78rem; text-transform: uppercase; letter-spacing: .05em; }
    .pay-form { display: flex; gap: 8px; align-items: center; }
    .pay-form select { padding: 6px 10px; border-radius: 8px; border: 1px solid var(--sage); font: inherit; font-size: .82rem; }
    .btn-pay { width: auto; padding: 7px 14px; font-size: .82rem; }
    .badge { padding: 4px 10px; border-radius: 20px; font-size: .76rem; font-weight: 600; }
    .badge-EN_ATTENTE { background: #fef3c7; color: #92400e; }
    .badge-EFFECTUE   { background: #dcfce7; color: #166534; }
    .badge-ECHOUE     { background: #fee2e2; color: #991b1b; }
</style>

<div class="app-layout">
    <%@ include file="../includes/client/sidebar.jsp" %>

    <main class="app-main">
        <h1>Mes paiements & échéances</h1>
        <p style="color:var(--muted); margin:4px 0 20px;">Consultez l'historique de vos cotisations et réglez vos échéances en attente.</p>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success">${sessionScope.success}</div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty sessionScope.error}">
            <div class="alert alert-error">${sessionScope.error}</div>
            <c:remove var="error" scope="session"/>
        </c:if>

        <table class="data-table">
            <thead>
                <tr>
                    <th>Référence</th>
                    <th>Contrat</th>
                    <th>Montant</th>
                    <th>Échéance</th>
                    <th>Date règlement</th>
                    <th>Moyen</th>
                    <th>Statut</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="p" items="${payments}">
                    <tr>
                        <td><strong>${p.transactionReference}</strong></td>
                        <td>${p.contract.contractNumber}</td>
                        <td><strong>${p.amount} €</strong></td>
                        <td>${p.dueDate}</td>
                        <td>${not empty p.paymentDate ? p.paymentDate.toLocalDate() : '—'}</td>
                        <td>${not empty p.paymentMethod ? p.paymentMethod : '—'}</td>
                        <td><span class="badge badge-${p.status}">${p.status}</span></td>
                        <td>
                            <c:choose>
                                <c:when test="${p.status == 'EN_ATTENTE' || p.status == 'ECHOUE'}">
                                    <form action="${pageContext.request.contextPath}/client/payments" method="post" class="pay-form">
                                        <input type="hidden" name="paymentId" value="${p.id}">
                                        <select name="paymentMethod" required>
                                            <c:forEach var="m" items="${paymentMethods}">
                                                <option value="${m}">${m}</option>
                                            </c:forEach>
                                        </select>
                                        <button type="submit" class="btn btn-pay">Régler</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <span style="color:var(--sage-dark); font-size:.85rem;">✓ Acquitté</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>