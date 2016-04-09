<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Home</title>
    <jsp:include page="../include/include.jsp"/>
    <script src="${pageContext.request.contextPath}/resources/js/managedocument.js"></script>
    <script src="${pageContext.request.contextPath}/resources/js/templateHandler.js"></script>
    <style>
        .big-check-box {
            width: 18px;
            height: 18px;
        }
        .checkbox-table {
            border-spacing: 10px;
            border-collapse: separate;
        }
    </style>
</head>

<body>

<jsp:include page="../include/header.jsp"/>
<jsp:include page="../include/sidebar.jsp"/>

<div class="container" style="width: 900px;">
    <form action="/document/upload" method="post" enctype="multipart/form-data">
        <label class="col-md-3 control-lable" for="files">Upload a document</label>
        <input type="file" multiple name="files[]" id="files" class="form-control input-sm"/>
        <br>
        <label class="col-md-3 control-lable" for="changedBy">Description</label>
        <input type="text" name="changedBy" id="changedBy" class="form-control input-sm"/>
        <br>
        <div class="form-actions floatRight">
            <input type="submit" value="Upload" class="btn btn-primary btn-sm">
            <button type="button" class="btn btn-default btn-sm make-dir-btn"
                    data-toggle="modal" data-target="#makeDirDialog">Make dir</button>
        </div>
    </form>
</div>

<div id="location" class="container" style="width: 900px;">
    <h3>${currentLocation}</h3>
</div>

<%--<div class="container" style="width: 900px;">--%>
    <%--<form action="/document/search">--%>
        <%--<input type="text" name="search" class="form-control input-sm">--%>
        <%--<input type="submit" value="Search" class="btn btn-primary btn-sm">--%>
    <%--</form>--%>
<%--</div>--%>

<div class="container" style="width: 900px;">
    <c:forEach items="${tableNames}" var="tableName">
        <table class="table table-hover tbody tr:hover td doc-table ${tableName}">
            <caption>
                <h3></h3>
                <a href="#" class="switch-btn all-href">All &nbsp</a>
                <a href="#" class="switch-btn public-href">Public &nbsp</a>
                <a href="#" class="switch-btn for-friends-href">For Friends &nbsp</a>
                <a href="#" class="switch-btn private-href">Private</a>

                <button class="btn btn-default delete-btn action-btn"
                        data-toggle="modal" data-target="#deleteDialog">Delete</button>
                <button class="btn btn-default replace-btn action-btn">Replace</button>
                <button class="btn btn-default copy-btn action-btn">Copy</button>
                <button class="btn btn-default rename-btn action-btn single-selection">Rename</button>
            </caption>
            <tr>
                <th><input type="checkbox" class="check-box big-check-box select-all"/></th>
                <th id="file-name">Name</th>
                <th>Size</th>
                <th>Changed</th>
                <th width="15"></th>
                <th width="15"></th>
            </tr>

            <c:forEach items="${directoriesMap[tableName]}" var="dir">
                <tr class="tr-dir${dir.id}">
                    <td width="20">
                        <input type="checkbox" class="check-box select select-dir big-check-box" value="${dir.id}"/>
                    </td>
                    <td class="directory-name">
                        <a href="<c:url value='/document/get-directory-content-${dir.hashName}' />">${dir.name}</a>
                    </td>
                    <td>--</td>
                    <td>--</td>
                    <td width="15"></td>
                    <td width="15">
                        <button type="button" class="btn btn-default btn-sm share-dir-btn"
                                data-toggle="modal" data-target="#shareDialog" value="${dir.id}">Share</button>
                    </td>
                </tr>
            </c:forEach>

            <c:forEach items="${documentsMap[tableName]}" var="doc" varStatus="counter">
                <tr class="tr-doc${doc.id}">
                    <td width="20">
                        <input type="checkbox" class="check-box select select-doc big-check-box" value="${doc.id}"/>
                    </td>
                    <td class="document-name">
                        <a href="/document/browse-${doc.id}">${doc.name}</a>
                    </td>
                    <td>${doc.size}</td>
                    <td class="document-date">
                        <fmt:formatDate type="date" timeStyle="short" dateStyle="short" value="${doc.lastModifyTime}"/>
                    </td>
                    <td width="15">
                        <a href="<c:url value='/document/download-${doc.id}'/>"
                           class="btn btn-default btn-sm custom-width">Download</a>
                    </td>
                    <td width="15">
                        <button type="button" class="btn btn-default btn-sm share-doc-btn"
                                data-toggle="modal" data-target="#shareDialog" value="${doc.id}">Share</button>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </c:forEach>
</div>

<div id="deleteDialog" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Removing</h4>
            </div>
            <div class="modal-body">
                <h4 id="delete-dialog-text"></h4>
            </div>
            <div class="modal-footer">
                <button type="button" id="deleteDocument" class="btn btn-success" data-dismiss="modal">YES</button>
                <button type="button" class="btn btn-danger" data-dismiss="modal">NO</button>
            </div>
        </div>

    </div>
</div>

<div id="makeDirDialog" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Make directory</h4>
            </div>
            <div class="modal-body">
                <p id="group-action">Input directory name</p>
                <input type="text" id="directoryName" class="form-control group-name-input"
                       placeholder="Directory Name" autofocus="">
            </div>
            <div class="modal-footer">
                <button type="button" id="makeDir" class="btn btn-default" data-dismiss="modal">Make dir</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>

<div id="shareDialog" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title"></h4>
            </div>

            <div class="modal-body">
                <div class="btn-group" data-toggle="buttons">
                    <input type="radio" name="access" id="PRIVATE" value="PRIVATE" checked>Private
                    <input type="radio" name="access" id="FOR_FRIENDS" value="FOR_FRIENDS">For friends
                    <input type="radio" name="access" id="PUBLIC" value="PUBLIC">Public
                </div>
                <table class="checkbox checkbox-table" id="friends-list">
                    <tr>
                        <th>Friends, who can read</th>
                        <th class="group-check-box">Friends, who can change</th>
                    </tr>
                    <c:forEach var="group" items="${friendsGroups}">
                        <tr class="group-${group.id}">
                            <td>
                                <label>
                                    <input type="checkbox" class="check-box readers-group-check-box" value="${group.id}">
                                        ${group.name}
                                </label>
                            </td>
                            <td class="group-check-box">
                                <label>
                                    <input type="checkbox" class="check-box editors-group-check-box" value="${group.id}">
                                        ${group.name}
                                </label>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:forEach var="friend" items="${friends}">
                        <tr class="group-${friend.id}">
                            <td>
                                <label>
                                    <input type="checkbox" class="check-box reader-check-box" value="${friend.id}">
                                        ${friend.firstName} ${friend.lastName}
                                </label>
                            </td>
                            <td class="group-check-box">
                                <label>
                                    <input type="checkbox" class="check-box editor-check-box" value="${friend.id}">
                                        ${friend.firstName} ${friend.lastName}
                                </label>
                            </td>
                        </tr>
                    </c:forEach>
                </table>
            </div>
            <div class="modal-footer">
                <button type="button" id="shareDocument" class="btn btn-default" data-dismiss="modal">SHARE</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
            </div>
        </div>

    </div>
</div>

</body>
</html>