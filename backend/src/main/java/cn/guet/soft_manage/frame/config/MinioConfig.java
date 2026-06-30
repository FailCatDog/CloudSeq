package cn.guet.soft_manage.frame.config;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端
 */
@Configuration
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .region(properties.getRegion())
                .build();
    }
}
