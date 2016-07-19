package com.geekhub.interceptors;

import com.geekhub.entities.User;
import com.geekhub.entities.UserDirectory;
import com.geekhub.interceptors.utils.InterceptorUtil;
import com.geekhub.security.FileAccessService;
import com.geekhub.services.UserDirectoryService;
import com.geekhub.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.function.BiPredicate;

public class DirectoryAccessInterceptor extends AccessInterceptor<UserDirectory> {

    @Inject
    private UserService userService;

    @Inject
    private UserDirectoryService userDirectoryService;

    @Inject
    private FileAccessService fileAccessService;

    @PostConstruct
    public void init() {

    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        Long userId = (Long) req.getSession().getAttribute("userId");
        Map<String, String> pathVariables = (Map) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (userId == null || CollectionUtils.isEmpty(pathVariables)) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }
        if (pathVariables.get("dirId") == null && pathVariables.get("dirHashName") == null) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;
        }
        if (pathVariables.get("dirId") != null) {
            String dirId = pathVariables.get("dirId");
            String url = InterceptorUtil.removeVariablesFromURI(req, pathVariables);
            if (permitAccess(Long.valueOf(dirId), userId, url)) {
                return true;
            }
        }
        if (pathVariables.get("dirHashName") != null) {
            String dirHashName = pathVariables.get("dirHashName");
            String url = InterceptorUtil.removeVariablesFromURI(req, pathVariables);
            if (permitAccess(dirHashName, userId, url)) {
                return true;
            }
        }
        resp.setStatus(HttpStatus.FORBIDDEN.value());
        return false;
    }

    @Override
    public boolean permitAccess(Long dirId, Long userId, String url) {
        BiPredicate<User, UserDirectory> predicate = getPredicate(url);
        User user = userService.getById(userId);
        UserDirectory directory = userDirectoryService.getById(dirId);
        return predicate == null || fileAccessService.permitAccess(directory, user, predicate);
    }

    public boolean permitAccess(String dirHashName, Long userId, String url) {
        BiPredicate<User, UserDirectory> predicate = getPredicate(url);
        User user = userService.getById(userId);
        UserDirectory directory = userDirectoryService.getByHashName(dirHashName);
        return predicate == null || fileAccessService.permitAccess(directory, user, predicate);
    }
}
