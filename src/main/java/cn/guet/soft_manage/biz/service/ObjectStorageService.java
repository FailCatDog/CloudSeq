package cn.guet.soft_manage.biz.service;

import java.io.InputStream;

/**
 * 对象存储抽象（MinIO 实现）
 */
public interface ObjectStorageService {

    void putObject(String storageKey, InputStream inputStream, long size, String contentType);

    InputStream getObject(String storageKey);

    void deleteObject(String storageKey);

    boolean exists(String storageKey);

    /** 生成供 OnlyOffice 直接拉取文件的预签名 URL */
    String getPresignedDownloadUrl(String storageKey, long expireSeconds);
}
