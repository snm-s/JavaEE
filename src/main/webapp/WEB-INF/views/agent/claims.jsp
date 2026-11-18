<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .filter-bar { display: flex; gap: 8px; margin: 16px 0 20px; flex-wrap: wrap; }
    .filter-pill { padding: 6px 14px; border-radius: 20px; border: 1px solid var(--sage); font-size: .82rem; color: var(--sage-dark); background: #fff; }
    .filter-pill.active { background: var(--bordeaux); color: #fff; border-color: var(--bordeaux); }
    .data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: var(--radius); overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,.04); }
    .data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid var(--sage-light); font-size: .9rem; }
    .data-table th { background: rgba(107, 30, 46, .08); color: var(--bordeaux); font-size: .78rem; text-transform: uppercase; letter-spacing: .05em; }
    .badge { padding: 4px 10px; border-radius: 20px; font-size: .76rem; font-weight: 600; }
    .badge-DECLARE    { background: #fef3c7; color: #92400e; }
    .badge-EN_ANALYSE { background: #e0f2fe; color: #075985; }
    .badge-ACCEPTE    { background: #dcfce7; color: #166534; }
    .badge-REFUSE     { background: #fee2e2; color: #991b1b; }
    .badge-INDEMNISE  { background: #e6eee2; color: var(--sage-dark); }
</style>

<div class="app-layout">
    <%@ include file="../includes/agent/sidebar.jsp" %>

    <main class="app-main">
        <h1>Instruction des sinistres</h1>
        <p style="color:var(--muted); margin:4px 0 0;">Analysez les déclarations, vérifiez les pièces justificatives et statuez sur l'indemnisation.</p>

        <div class="filter-bar">
            <a href="${pageContext.request.contextPath}/agent/claims" class="filter-pill ${empty selectedStatus ? 'active' : ''}">Tous</a>
            <c:forEach var="st" items="${statuses}">
                <a href="${pageContext.request.contextPath}/agent/claims?status=${st}" class="filter-pill ${selectedStatus == st.name() ? 'active' : ''}">${st}</a>
            </c:forEach>
        </div>

        <table class="data-table">
            <thead>
                <tr>
                    <th>Numéro</th>
                    <th>Contrat</th>
                    <th>Type</th>
                    <th>Date incident</th>
                    <th>Estimation</th>
                    <th>Indemnité</th>
                    <th>Statut</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="c" items="${claims}">
                    <tr>
                        <td><strong>${c.claimNumber}</strong></td>
                        <td>${c.contract.contractNumber}</td>
                        <td>${c.claimType}</td>
                        <td>${c.incidentDate}</td>
                        <td>${c.estimatedAmount} €</td>
                        <td>${not empty c.indemnityAmount ? c.indemnityAmount.toString().concat(' €') : '—'}</td>
                        <td><span class="badge badge-${c.status}">${c.status}</span></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/agent/claims?id=${c.id}" class="btn" style="display:inline-block; width:auto; padding:6px 12px; font-size:.8rem;">Instruire</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>