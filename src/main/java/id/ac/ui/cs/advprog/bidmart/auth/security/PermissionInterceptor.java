package id.ac.ui.cs.advprog.bidmart.auth.security;

import id.ac.ui.cs.advprog.bidmart.auth.exception.ForbiddenPermissionException;
import id.ac.ui.cs.advprog.bidmart.auth.service.PermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private final PermissionService permissionService;

    public PermissionInterceptor(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresPermission requirement = AnnotatedElementUtils.findMergedAnnotation(
            handlerMethod.getMethod(),
            RequiresPermission.class
        );
        if (requirement == null) {
            requirement = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), RequiresPermission.class);
        }
        if (requirement == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenPermissionException();
        }

        UUID userId = UUID.fromString(authentication.getName());
        if (!permissionService.hasAnyAllowedAndNoForbidden(userId, requirement.allowed(), requirement.forbidden())) {
            throw new ForbiddenPermissionException();
        }
        return true;
    }
}
