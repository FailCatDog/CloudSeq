package cn.guet.soft_manage.frame.interceptor;

import cn.guet.soft_manage.biz.rbac.entity.SysMenu;
import cn.guet.soft_manage.biz.rbac.service.IPermissionService;
import cn.guet.soft_manage.biz.rbac.support.ApiPathMatcher;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Set;

/**
 * RBAC API 权限拦截器：匹配菜单表 api_path + api_method，校验 perms
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final long RULE_CACHE_TTL_MS = 60_000L;

    @Resource
    private IPermissionService permissionService;

    private volatile List<SysMenu> cachedRules = List.of();
    private volatile long cachedAtMs = 0L;

    @PostConstruct
    public void warmUpRules() {
        refreshRules();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestPath = ApiPathMatcher.normalizePath(request.getRequestURI());
        if (shouldBypass(requestPath)) {
            return true;
        }

        List<SysMenu> rules = currentRules();
        if (rules.isEmpty()) {
            return true;
        }

        String requestMethod = request.getMethod();
        SysMenu matchedRule = findMatchingRule(rules, requestPath, requestMethod);
        if (matchedRule == null) {
            return true;
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        String requiredPerm = matchedRule.getPerms();
        if (!StringUtils.hasText(requiredPerm)) {
            return true;
        }

        Set<String> ownedPerms = permissionService.getPermissionCodes(userId);
        if (ownedPerms.contains(requiredPerm.trim())) {
            return true;
        }

        log.warn("permission denied userId={} {} {} requiredPerm={}", userId, requestMethod, requestPath, requiredPerm);
        throw new BusinessException(BizResponseCode.FORBIDDEN);
    }

    private List<SysMenu> currentRules() {
        long now = System.currentTimeMillis();
        if (now - cachedAtMs > RULE_CACHE_TTL_MS) {
            refreshRules();
        }
        return cachedRules;
    }

    private synchronized void refreshRules() {
        cachedRules = permissionService.listGlobalApiMenuRules();
        cachedAtMs = System.currentTimeMillis();
        log.info("loaded {} api permission rules", cachedRules.size());
    }

    /** 菜单变更后立即刷新 API 规则缓存 */
    public void refreshRulesNow() {
        refreshRules();
    }

    private SysMenu findMatchingRule(List<SysMenu> rules, String requestPath, String requestMethod) {
        for (SysMenu rule : rules) {
            if (ApiPathMatcher.methodMatches(rule.getApiMethod(), requestMethod)
                    && ApiPathMatcher.pathMatches(rule.getApiPath(), requestPath)) {
                return rule;
            }
        }
        return null;
    }

    private boolean shouldBypass(String requestPath) {
        if (requestPath.startsWith("/api/dict")
                || requestPath.startsWith("/api/internal/collab")
                || requestPath.startsWith("/api/assets")
                || requestPath.startsWith("/static")) {
            return true;
        }
        // 登录用户自助账号接口：JWT 已鉴权，不再走按钮权限规则
        if (requestPath.equals("/api/account/profile/status")
                || requestPath.equals("/api/account/permissions")
                || requestPath.equals("/api/account/profile")) {
            return true;
        }
        // 管理端：Service 层 AdminAuthSupport 校验 ADMIN 角色
        return requestPath.startsWith("/api/admin");
    }
}
