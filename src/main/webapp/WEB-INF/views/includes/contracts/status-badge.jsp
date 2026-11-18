<%-- Badge colore d'un statut de contrat (inclus plusieurs fois par page : pas de directive page,
     accents ecrits en entites HTML). Avant l'include : <c:set var="st" value="${...status}"/> --%>
<span class="ct-badge ct-${st}"><c:choose><c:when test="${st == 'EN_ATTENTE'}">En attente</c:when><c:when test="${st == 'ACTIF'}">Actif</c:when><c:when test="${st == 'SUSPENDU'}">Suspendu</c:when><c:when test="${st == 'EXPIRE'}">Expir&eacute;</c:when><c:when test="${st == 'RESILIE'}">R&eacute;sili&eacute;</c:when><c:otherwise>${st}</c:otherwise></c:choose></span>
