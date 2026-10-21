package cn.guet.soft_manage.biz.rbac.support;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

/**
 * API 路径与 HTTP 方法匹配（Ant 风格，支持 * 与 **）
 */
public final class ApiPathMatcher {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private ApiPathMatcher() {
    }

    public static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/";
        }
        String normalized = path.split("\\?")[0].split("#")[0];
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public static boolean pathMatches(String pattern, String requestPath) {
        if (!StringUtils.hasText(pattern) || !StringUtils.hasText(requestPath)) {
            return false;
        }
        return PATH_MATCHER.match(normalizePath(pattern), normalizePath(requestPath));
    }

    public static boolean methodMatches(String ruleMethod, String requestMethod) {
        if (!StringUtils.hasText(ruleMethod)) {
            return true;
        }
        if (!StringUtils.hasText(requestMethod)) {
            return false;
        }
        return ruleMethod.trim().equalsIgnoreCase(requestMethod.trim());
    }
}
