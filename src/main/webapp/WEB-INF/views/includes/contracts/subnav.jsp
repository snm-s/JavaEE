<%-- Navigation interne du module (espaces agent et admin) --%>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="uri" value="${requestScope['jakarta.servlet.forward.request_uri']}"/>
<nav class="ct-subnav">
    <a href="${ctx}/${role}/contracts" class="${fn:contains(uri, '/contracts') and not fn:contains(uri, '/new') ? 'active' : ''}">Contrats</a>
    <c:if test="${role == 'agent'}">
        <a href="${ctx}/agent/contracts/new" class="${fn:contains(uri, '/new') ? 'active' : ''}">+ Nouveau contrat</a>
    </c:if>
    <a href="${ctx}/${role}/guarantees" class="${fn:contains(uri, '/guarantees') ? 'active' : ''}">Garanties</a>
    <c:if test="${role == 'admin'}">
        <a href="${ctx}/admin/insurance-types" class="${fn:contains(uri, '/insurance-types') ? 'active' : ''}">Types d'assurance</a>
    </c:if>
</nav>
