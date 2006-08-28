<%--
  ~ Copyright 2005-2006 The Apache Software Foundation.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="ww" uri="/webwork" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>Administration</title>
  <ww:head/>
</head>

<body>

<h1>Administration</h1>

<div id="contentArea">
<h2>Configuration</h2>

<table>
  <tr>
    <th>Index Directory</th>
    <td>
      <ww:property value="indexPath"/>
    </td>
    <td></td>
  </tr>
  <tr>
    <th>Indexing Schedule</th>
    <td>
      <ww:property value="indexerCronExpression"/>
    </td>
    <%-- TODO: a "run now without timestamp checking" operation should be here too, to pick up any stragglers (in the event of a bug) --%>
    <%-- TODO: a "delete index and run now" operation should be here too (really clean, remove deletions that didn't get picked up) --%>
    <td><a href="<ww:url action="runIndexer" />">Run Now</a></td>
  </tr>
</table>

<ww:set name="proxy" value="proxy"/>
<c:if test="${!empty(proxy.host)}">
  <h3>HTTP Proxy</h3>

  <table>
    <tr>
      <th>Host</th>
      <td>${proxy.host}</td>
    </tr>
    <tr>
      <th>Port</th>
      <td>${proxy.port}</td>
    </tr>
    <tr>
      <th>Username</th>
      <td>${proxy.username}</td>
    </tr>
  </table>
</c:if>

<p>
  <a href="<ww:url action="configure" />">Edit Configuration</a>
</p>

<h2>Managed Repositories</h2>

<ww:set name="repositories" value="repositories"/>
<c:if test="${empty(repositories)}">
  <strong>There are no managed repositories configured yet.</strong>
</c:if>
<c:forEach items="${repositories}" var="repository" varStatus="i">
  <div>
    <div style="float: right">
        <%-- TODO replace with icons --%>
      <a href="<ww:url action="editRepository" method="input"><ww:param name="repoId" value="%{'${repository.id}'}" /></ww:url>">Edit
        Repository</a> | <a
        href="<ww:url action="deleteRepository" method="input"><ww:param name="repoId" value="%{'${repository.id}'}" /></ww:url>">Delete
      Repository</a>
    </div>
    <h3>${repository.name}</h3>
    <table>
      <tr>
        <th>Identifier</th>
        <td>
          <code>${repository.id}</code>
        </td>
      </tr>
      <tr>
        <th>Directory</th>
        <td>${repository.directory}</td>
      </tr>
      <tr>
        <th>Type</th>
          <%-- TODO: can probably just use layout appended to a key prefix in i18n to simplify this --%>
        <td>
          <c:choose>
            <c:when test="${repository.layout == 'default'}">
              Maven 2.x Repository
            </c:when>
            <c:otherwise>
              Maven 1.x Repository
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
      <tr>
        <th>Snapshots Included</th>
        <td class="${repository.includeSnapshots ? 'doneMark' : 'errorMark'}"></td>
      </tr>
      <tr>
        <th>Indexed</th>
        <td class="${repository.indexed ? 'doneMark' : 'errorMark'}"></td>
      </tr>
    </table>
  </div>
</c:forEach>

<p>
  <a href="<ww:url action="addRepository" method="input" />">Add Repository</a>
</p>
</div>

</body>
</html>