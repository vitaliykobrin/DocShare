$(document).ready(function() {
    var documentUploadUrl = '/document/upload';
    var searchUrl = '/main/search';
    var profileUrl = '/profile';
    var currentUrl = window.location.href;

    if (currentUrl.indexOf(documentUploadUrl) > 0) {
        $('#documentUpload').addClass('active');
    } else if (currentUrl.indexOf(searchUrl) > 0) {
        $('#search').addClass('active');
    } else if (currentUrl.indexOf(profileUrl) > 0) {
        $('#profile').addClass('active');
    }
});