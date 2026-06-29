package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 协同编辑配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "collab")
public class CollabProperties {

    private String wsUrl = "ws://localhost:1234";

    private String jwtSecret = "soft_manage-collab-jwt-secret-change-me";

    private long tokenExpireSeconds = 600L;

    private String roomPrefix = "doc-";

    private String internalSecret = "soft_manage-collab-internal-secret-change-me";
}
