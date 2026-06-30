package cn.guet.soft_manage.frame.storage;

import java.io.InputStream;

/**
 * 对象存储抽象（当前实现为 MinIO）
 */
public interface ObjectStorageService {

    /** 连通性检查：列出桶或访问指定桶 */
    boolean ping();

    /** 确保 {@link cn.guet.soft_manage.frame.constant.MinioConstants#REQUIRED_BUCKETS} 已创建 */
    void ensureRequiredBuckets();

    /**
     * 上传对象
     *
     * @param bucket      桶名，建议使用 {@link cn.guet.soft_manage.frame.constant.MinioConstants} 中的常量
     * @param objectKey   对象 key
     * @param inputStream 内容流（由调用方打开，本方法内读取并关闭）
     * @param size        字节数
     * @param contentType MIME，可为 null
     */
    void putObject(String bucket, String objectKey, InputStream inputStream, long size, String contentType);

    InputStream getObject(String bucket, String objectKey);

    void removeObject(String bucket, String objectKey);

    boolean bucketExists(String bucket);
}
