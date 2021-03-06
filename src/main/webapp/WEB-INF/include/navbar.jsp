<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="/api/home">DocShare</a>
        </div>
        <ul class="nav navbar-nav">
            <li id="documentUpload"><a href="/api/home">Home</a></li>
            <li id="search"><a href="/api/search/page">Search Friends</a></li>
            <li id="profile"><a href="/api/profile/">Profile</a></li>

            <c:if test="${renderComments}">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true"
                       aria-expanded="false">Settings<span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <li class="showCommentLi">
                            <a href="#" class="comment-box-visible">Hide comment box</a>
                        </li>
                        <c:if test="${abilityToComment != null}">
                            <c:if test="${abilityToComment}">
                                <li>
                                    <a href="#" class="off-comments">Disable comments for this file</a>
                                </li>
                            </c:if>
                            <c:if test="${!abilityToComment}">
                                <li>
                                    <a href="#" class="on-comments">Enable comments for this file</a>
                                </li>
                            </c:if>
                            <li>
                                <a href="#" class="clear-comments">Delete all comments</a>
                            </li>
                        </c:if>
                    </ul>
                </li>
            </c:if>
        </ul>
        <form class="navbar-form navbar-left form-search" role="search" hidden>
            <div class="form-group">
                <input type="text" class="form-control" placeholder="Search files" id="filesSearch">
            </div>
            <button type="button" id="searchButton" class="btn btn-default">Submit</button>
        </form>
        <ul class="nav navbar-nav navbar-right">
            <li><a href="/api/sign_out">Sign Out</a></li>
        </ul>
    </div>
</nav>
