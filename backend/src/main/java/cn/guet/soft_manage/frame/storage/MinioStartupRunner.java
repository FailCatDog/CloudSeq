package cn.guet.soft_manage.frame.storage;

import cn.guet.soft_manage.frame.config.MinioProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 启动时检查 MinIO 连通性并创建所需桶
 */
@Slf4j
@Component
@ConditionalOnBean(ObjectStorageService.class)
public class MinioStartupRunner implements ApplicationRunner {

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private ObjectStorageService objectStorageService;

    @Override
    public void run(ApplicationArguments args) {
        if (!minioProperties.isEnabled()) {
            log.info("MinIO is disabled, skip startup check");
            return;
        }

        log.info("Checking MinIO connection: {}", minioProperties.getEndpoint());
        if (!objectStorageService.ping()) {
            log.error("MinIO is unreachable at {}, please check minio.* config and service status",
                    minioProperties.getEndpoint());
            return;
        }

        objectStorageService.ensureRequiredBuckets();
        log.info("MinIO is ready, buckets: workspace-assets, workspace-exports");
    }
}
