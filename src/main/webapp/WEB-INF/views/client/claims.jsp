<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="../includes/header.jsp" %>
<%@ include file="../includes/navbar.jsp" %>

<style>
    .header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
    .btn-sm { display: inline-block; width: auto; padding: 10px 18px; font-size: .88rem; }
    .btn-outline { background: transparent; color: var(--bordeaux); border: 1px solid var(--bordeaux); }
    .btn-outline:hover { background: var(--bordeaux); color: #fff; }
    .data-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: var(--radius); overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,.04); }
    .data-table th, .data-table td { padding: 14px 16px; text-align: left; border-bottom: 1px solid var(--sage-light); font-size: .92rem; }
    .data-table th { background: rgba(125, 149, 119, .15); color: var(--sage-dark); font-size: .78rem; text-transform: uppercase; letter-spacing: .05em; }
    .badge { padding: 4px 10px; border-radius: 20px; font-size: .76rem; font-weight: 600; display: inline-block; }
    .badge-DECLARE    { background: #fef3c7; color: #92400e; }
    .badge-EN_ANALYSE { background: #e0f2fe; color: #075985; }
    .badge-ACCEPTE    { background: #dcfce7; color: #166534; }
    .badge-REFUSE     { background: #fee2e2; color: #991b1b; }
    .badge-INDEMNISE  { background: #e6eee2; color: var(--sage-dark); border: 1px solid var(--sage); }
</style>

<div class="app-layout">
    <%@ include file="../includes/client/sidebar.jsp" %>

    <main class="app-main">
        <div class="header-row">
            <div>
                <h1>Mes sinistres</h1>
                <p style="color:var(--muted); margin:4px 0 0;">Déclarez un incident et suivez l'avancement de vos indemnisations.</p>
            </div>
            <a href="${pageContext.request.contextPath}/client/claims?action=new" class="btn btn-sm">+ Déclarer un sinistre</a>
        </div>

        <c:if test="${not empty sessionScope.success}">
            <div class="alert alert-success">${sessionScope.success}</div>
            <c:remove var="success" scope="session"/>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <c:choose>
            <c:when test="${empty claims}">
                <div class="card" style="max-width:100%; text-align:center;">
                    <p style="color:var(--muted);">Vous n'avez déclaré aucun sinistre pour le moment.</p>
                </div>
            </c:when>
            <c:otherwise>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Référence</th>
                            <th>Contrat</th>
                            <th>Type</th>
                            <th>Date incident</th>
                            <th>Estimation</th>
                            <th>Indemnité validée</th>
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
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty c.indemnityAmount}">${c.indemnityAmount} €</c:when>
                                        <c:otherwise><span style="color:var(--muted);">—</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><span class="badge badge-${c.status}">${c.status}</span></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/client/claims?id=${c.id}" class="btn btn-sm btn-outline" style="padding:6px 12px; font-size:.8rem;">Détails</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<%@ include file="../includes/footer.jsp" %>