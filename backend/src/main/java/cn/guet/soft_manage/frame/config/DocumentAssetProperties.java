package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档资产上传限制
 */
@Data
@Component
@ConfigurationProperties(prefix = "document.asset")
public class DocumentAssetProperties {

    /** 单文件最大字节数，默认 10MB */
    private long maxSizeBytes = 10L * 1024L * 1024L;

    /** 允许的 MIME 类型 */
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf"
    );
}
