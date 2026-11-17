
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="../includes/header.jsp" />
<jsp:include page="../includes/navbar.jsp" />

<div class="app-layout">
    <jsp:include page="../includes/client/sidebar.jsp" />

    <main class="app-main">
        <div class="contracts-header">
            <h1>Mes contrats</h1>
            <p>Retrouvez et gérez vos contrats d'assurance.</p>
        </div>

        <c:choose>
            <c:when test="${empty contracts}">
                <div class="contracts-empty">
                    <h2>Aucun contrat pour le moment</h2>
                    <p>Vos contrats d'assurance apparaîtront ici.</p>
                </div>
            </c:when>

            <c:otherwise>
                <div class="contracts-grid">
                    <c:forEach var="contract" items="${contracts}">
                        <article class="contract-card">

                            <div class="contract-card-top">
                                <span class="contract-icon">◆</span>

                                <span class="contract-status status-${contract.status}">
                                    <c:choose>
                                        <c:when test="${contract.status == 'ACTIF'}">Actif</c:when>
                                        <c:when test="${contract.status == 'EN_ATTENTE'}">En attente</c:when>
                                        <c:when test="${contract.status == 'EXPIRE'}">Expiré</c:when>
                                        <c:when test="${contract.status == 'RESILIE'}">Résilié</c:when>
                                        <c:when test="${contract.status == 'SUSPENDU'}">Suspendu</c:when>
                                        <c:otherwise>${contract.status}</c:otherwise>
                                    </c:choose>
                                </span>

                            </div>

                            <h2>${contract.insuranceType.name}</h2>

                            <p class="contract-number">
                                Contrat n° ${contract.contractNumber}
                            </p>

                            <p class="contract-date">
                                Échéance : ${contract.endDate}
                            </p>

                            <p class="contract-premium">
                                Prime : ${contract.premiumAmount} €
                            </p>

                            <a class="contract-details-btn"
                               href="${pageContext.request.contextPath}/client/contracts/${contract.id}">
                                Voir les détails →
                            </a>

                        </article>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </main>
</div>

<jsp:include page="../includes/footer.jsp" />