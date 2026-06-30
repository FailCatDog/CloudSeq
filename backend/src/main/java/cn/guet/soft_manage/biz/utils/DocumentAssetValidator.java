package cn.guet.soft_manage.biz.utils;

import cn.guet.soft_manage.frame.config.DocumentAssetProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

/**
 * 文档资产上传校验
 */
public final class DocumentAssetValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf"
    );

    private DocumentAssetValidator() {
    }

    public static void validateUpload(MultipartFile file, DocumentAssetProperties properties) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BizResponseCode.FILE_EMPTY);
        }

        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new BusinessException(BizResponseCode.FILE_TOO_LARGE);
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!properties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(BizResponseCode.FILE_TYPE_NOT_ALLOWED);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(BizResponseCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    public static String resolveAssetType(String contentType) {
        if (contentType != null && contentType.startsWith("image/")) {
            return "IMAGE";
        }
        return "ATTACHMENT";
    }

    public static String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        String name = originalFilename.trim();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        return name.substring(dot).toLowerCase(Locale.ROOT);
    }

    public static String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }
}
