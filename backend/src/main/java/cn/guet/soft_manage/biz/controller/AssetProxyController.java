package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.DocumentAssetReadDTO;
import cn.guet.soft_manage.biz.service.DocumentAssetService;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 文档资产读取（后端代理从 MinIO 流式输出）
 */
@RestController
@RequestMapping("/api/assets")
@ConditionalOnBean(DocumentAssetService.class)
public class AssetProxyController {

    @Resource
    private DocumentAssetService documentAssetService;

    @GetMapping("/{assetId}")
    public ResponseEntity<StreamingResponseBody> readAsset(@PathVariable Long assetId) {
        DocumentAssetReadDTO asset = documentAssetService.readAsset(assetId);
        if (asset.getInputStream() == null) {
            throw new BusinessException(BizResponseCode.ASSET_NOT_FOUND);
        }

        MediaType mediaType = resolveMediaType(asset.getContentType());
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(asset.getFileName(), StandardCharsets.UTF_8)
                .build();

        StreamingResponseBody body = outputStream -> {
            try (InputStream inputStream = asset.getInputStream()) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
                .body(body);
    }

    private static MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
