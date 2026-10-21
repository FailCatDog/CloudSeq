package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 连接配置（暂存 application.yml，后期可迁至 Nacos）
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** 是否启用对象存储；关闭时跳过 MinIO 客户端与启动自检 */
    private boolean enabled = true;

    /** MinIO API 地址，如 http://127.0.0.1:9000 */
    private String endpoint = "http://127.0.0.1:9000";

    private String accessKey = "minioadmin";

    private String secretKey = "minioadmin";

    /** S3 兼容 region，MinIO 默认即可 */
    private String region = "us-east-1";
}
