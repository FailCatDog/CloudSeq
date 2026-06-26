package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.service.ObjectStorageService;
import cn.guet.soft_manage.frame.config.MinioProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;

/**
 * MinIO 对象存储实现
 */
@Slf4j
@Service
public class MinioObjectStorageService implements ObjectStorageService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    @Override
    public void putObject(String storageKey, InputStream inputStream, long size, String contentType) {
        requireKey(storageKey);
        ensureBucket();
        try {
            long objectSize = size >= 0 ? size : -1;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(storageKey)
                    .stream(inputStream, objectSize, -1)
                    .contentType(contentType)
                    .build());
        } catch (ErrorResponseException ex) {
            log.error("MinIO putObject failed: bucket={}, key={}, code={}, message={}",
                    minioProperties.getBucket(), storageKey,
                    ex.errorResponse().code(), ex.errorResponse().message());
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        } catch (Exception ex) {
            log.error("MinIO putObject failed: bucket={}, key={}", minioProperties.getBucket(), storageKey, ex);
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }

    @Override
    public InputStream getObject(String storageKey) {
        requireKey(storageKey);
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(storageKey)
                    .build());
            return response;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())) {
                throw new BusinessException(BizResponseCode.FILE_NOT_FOUND);
            }
            log.error("MinIO getObject failed: bucket={}, key={}, code={}",
                    minioProperties.getBucket(), storageKey, ex.errorResponse().code());
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        } catch (Exception ex) {
            log.error("MinIO getObject failed: bucket={}, key={}", minioProperties.getBucket(), storageKey, ex);
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }

    @Override
    public void deleteObject(String storageKey) {
        requireKey(storageKey);
        try {
            if (!exists(storageKey)) {
                return;
            }
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(storageKey)
                    .build());
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }

    @Override
    public boolean exists(String storageKey) {
        requireKey(storageKey);
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(storageKey)
                    .build());
            return true;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())) {
                return false;
            }
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }

    @Override
    public String getPresignedDownloadUrl(String storageKey, long expireSeconds) {
        requireKey(storageKey);
        ensureBucket();
        try {
            int expiry = (int) Math.min(Math.max(expireSeconds, 60L), 7 * 24 * 3600L);
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getBucket())
                    .object(storageKey)
                    .expiry(expiry)
                    .build());
        } catch (Exception ex) {
            log.error("MinIO presign failed: bucket={}, key={}", minioProperties.getBucket(), storageKey, ex);
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }

    private void requireKey(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
    }

    private void ensureBucket() {
        try {
            String bucket = minioProperties.getBucket();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            }
        } catch (Exception ex) {
            log.error("MinIO ensureBucket failed: bucket={}", minioProperties.getBucket(), ex);
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }
}
