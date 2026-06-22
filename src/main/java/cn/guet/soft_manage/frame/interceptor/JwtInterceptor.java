package cn.guet.soft_manage.frame.interceptor;

import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.utils.JwtUtil;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.constant.JwtConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
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
        String authHeader = request.getHeader(JwtConstants.TOKEN_HEADER);

        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(JwtConstants.TOKEN_PREFIX.length());

        try {
            User user = JwtUtil.getLoginUser(token);
            log.info("userId={} 正在请求...", user.getId());
            UserContext.set(user);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new BusinessException(BizResponseCode.TOKEN_EXPIRED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
