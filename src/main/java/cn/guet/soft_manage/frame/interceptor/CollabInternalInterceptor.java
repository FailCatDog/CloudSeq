package cn.guet.soft_manage.frame.interceptor;

import cn.guet.soft_manage.frame.config.CollabProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 协同服务内部 API 鉴权
 */
@Component
public class CollabInternalInterceptor implements HandlerInterceptor {

    public static final String INTERNAL_SECRET_HEADER = "X-Collab-Secret";

    @Resource
    private CollabProperties collabProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String secret = request.getHeader(INTERNAL_SECRET_HEADER);
        if (!StringUtils.hasText(secret) || !secret.equals(collabProperties.getInternalSecret())) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }
        return true;
    }
}
