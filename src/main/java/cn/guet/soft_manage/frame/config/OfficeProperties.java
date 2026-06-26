package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OnlyOffice 集成配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "office")
public class OfficeProperties {

    private String documentServerUrl = "http://localhost:8082";

    /** OnlyOffice 容器回调 Spring 时使用的基础 URL */
    private String callbackBaseUrl = "http://localhost:9001";

    private String jwtSecret = "soft_manage-onlyoffice-jwt-secret-change-me";

    private boolean jwtEnabled = true;

    /** OnlyOffice 拉取文档用的短期 download token 有效期（秒） */
    private long downloadTokenExpireSeconds = 3600L;
}
