package cn.guet.soft_manage.frame.storage;

import cn.guet.soft_manage.frame.config.MinioProperties;
import cn.guet.soft_manage.frame.constant.MinioConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * MinIO 对象存储实现
 */
@Slf4j
@Service
@ConditionalOnBean(MinioClient.class)
public class MinioObjectStorageService implements ObjectStorageService {

    /** MinIO 分片上传默认分片大小（10MB） */
    private static final long DEFAULT_PART_SIZE = 10L * 1024L * 1024L;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    @Override
    public boolean ping() {
        try {
            minioClient.listBuckets();
            return true;
        } catch (Exception ex) {
            log.warn("MinIO ping failed: {}", ex.getMessage());
            return false;
        }
    }

    @Override
    public void ensureRequiredBuckets() {
        for (String bucket : MinioConstants.REQUIRED_BUCKETS) {
            ensureBucket(bucket);
        }
    }

    @Override
    public void putObject(String bucket, String objectKey, InputStream inputStream, long size, String contentType) {
        assertBucketAndKey(bucket, objectKey);
        ensureBucket(bucket);
        try (inputStream) {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey);
            if (size > 0) {
                builder.stream(inputStream, size, -1);
            } else {
                builder.stream(inputStream, -1, DEFAULT_PART_SIZE);
            }
            if (StringUtils.hasText(contentType)) {
                builder.contentType(contentType);
            }
            minioClient.putObject(builder.build());
        } catch (Exception ex) {
            log.error("MinIO putObject failed, bucket={}, key={}, size={}, reason={}",
                    bucket, objectKey, size, ex.getMessage(), ex);
            throw new BusinessException(BizResponseCode.OBJECT_STORAGE_ERROR);
        }
    }

    @Override
    public InputStream getObject(String bucket, String objectKey) {
        assertBucketAndKey(bucket, objectKey);
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.error("MinIO getObject failed, bucket={}, key={}", bucket, objectKey, ex);
            throw new BusinessException(BizResponseCode.OBJECT_STORAGE_ERROR);
        }
    }

    @Override
    public void removeObject(String bucket, String objectKey) {
        assertBucketAndKey(bucket, objectKey);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.error("MinIO removeObject failed, bucket={}, key={}", bucket, objectKey, ex);
            throw new BusinessException(BizResponseCode.OBJECT_STORAGE_ERROR);
        }
    }

    @Override
    public boolean bucketExists(String bucket) {
        if (!StringUtils.hasText(bucket)) {
            return false;
        }
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        } catch (Exception ex) {
            log.warn("MinIO bucketExists failed, bucket={}: {}", bucket, ex.getMessage());
            return false;
        }
    }

    private void ensureBucket(String bucket) {
        if (bucketExists(bucket)) {
            log.info("MinIO bucket already exists: {}", bucket);
            return;
        }
        try {
            MakeBucketArgs.Builder builder = MakeBucketArgs.builder().bucket(bucket);
            if (StringUtils.hasText(minioProperties.getRegion())) {
                builder.region(minioProperties.getRegion());
            }
            minioClient.makeBucket(builder.build());
            log.info("MinIO bucket created: {}", bucket);
        } catch (Exception ex) {
            log.error("MinIO makeBucket failed, bucket={}", bucket, ex);
            throw new BusinessException(BizResponseCode.OBJECT_STORAGE_ERROR);
        }
    }

    private static void assertBucketAndKey(String bucket, String objectKey) {
        if (!StringUtils.hasText(bucket) || !StringUtils.hasText(objectKey)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
    }
}
