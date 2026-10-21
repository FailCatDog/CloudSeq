package cn.guet.soft_manage.frame.config;

import cn.guet.soft_manage.frame.interceptor.CollabInternalInterceptor;
import cn.guet.soft_manage.frame.interceptor.JwtInterceptor;
import cn.guet.soft_manage.frame.interceptor.PermissionInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: Web MVC 通用配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private CollabInternalInterceptor collabInternalInterceptor;

    @Resource
    private PermissionInterceptor permissionInterceptor;

    private static final String[] AUTH_EXCLUDE_PATTERNS = {
            "/**/login",
            "/**/register",
            "/api/dict/**",
            "/api/internal/collab/**"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 后续可在这里注册自定义转换器/格式化器
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(collabInternalInterceptor)
                .addPathPatterns("/api/internal/collab/**");

        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(AUTH_EXCLUDE_PATTERNS);

        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(AUTH_EXCLUDE_PATTERNS);
    }
}
