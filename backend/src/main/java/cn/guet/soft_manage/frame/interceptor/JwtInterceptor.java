package cn.guet.soft_manage.frame.interceptor;

import cn.guet.soft_manage.frame.auth.JwtUtil;
import cn.guet.soft_manage.frame.auth.LoginUser;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.constant.JwtConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 */
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        if (token == null || token.isBlank()) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        LoginUser loginUser = JwtUtil.parseLoginUser(token);
        log.info("userId={} 正在请求...", loginUser.getId());
        UserContext.set(loginUser);
        return true;
    }

    /**
     * 优先 Authorization 头；资产读取 GET 支持 query access_token，便于 img 标签加载
     */
    private static String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(JwtConstants.TOKEN_HEADER);
        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return authHeader.substring(JwtConstants.TOKEN_PREFIX.length());
        }

        if ("GET".equalsIgnoreCase(request.getMethod()) && isAssetReadPath(request.getRequestURI())) {
            String queryToken = request.getParameter("access_token");
            if (queryToken != null && !queryToken.isBlank()) {
                return queryToken;
            }
        }
        return null;
    }

    private static boolean isAssetReadPath(String uri) {
        return uri != null && uri.startsWith("/api/assets/");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
