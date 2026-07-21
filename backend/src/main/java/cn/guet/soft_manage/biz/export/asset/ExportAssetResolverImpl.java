package cn.guet.soft_manage.biz.export.asset;

import cn.guet.soft_manage.biz.document.dao.WorkspaceAssetDao;
import cn.guet.soft_manage.biz.document.entity.WorkspaceAsset;
import cn.guet.soft_manage.biz.document.util.DocumentAssetUrlHelper;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.frame.constant.MinioConstants;
import cn.guet.soft_manage.frame.storage.ObjectStorageService;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ExportAssetResolverImpl implements ExportAssetResolver {

    private static final Logger log = LoggerFactory.getLogger(ExportAssetResolverImpl.class);

    private final WorkspaceAssetDao assetDao;
    private final ObjectStorageService storage;
    private final SafeExternalImageFetcher externalImageFetcher;

    public ExportAssetResolverImpl(
        WorkspaceAssetDao assetDao,
        ObjectStorageService storage,
        SafeExternalImageFetcher externalImageFetcher
    ) {
        this.assetDao = assetDao;
        this.storage = storage;
        this.externalImageFetcher = externalImageFetcher;
    }

    @Override
    public ResolvedExportAsset resolve(Long workspaceId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        Long assetId = parseAssetId(imageUrl);
        if (assetId != null) {
            if (workspaceId == null) {
                return null;
            }
            return resolveWorkspaceAsset(workspaceId, assetId);
        }

        String decoded = Parser.unescapeEntities(imageUrl.trim(), false);
        if (looksLikeHttpUrl(decoded)) {
            return externalImageFetcher.fetch(decoded);
        }

        log.warn("export asset: cannot parse asset id from url={}", imageUrl);
        return null;
    }

    private ResolvedExportAsset resolveWorkspaceAsset(Long workspaceId, Long assetId) {
        WorkspaceAsset asset = assetDao.selectById(assetId);
        if (asset == null) {
            log.warn("export asset: asset not found id={}", assetId);
            return null;
        }
        if (!workspaceId.equals(asset.getWorkspaceId())) {
            log.warn("export asset: workspace mismatch assetId={} expected={} actual={}",
                assetId, workspaceId, asset.getWorkspaceId());
            return null;
        }

        try (InputStream inputStream = storage.getObject(
                MinioConstants.BUCKET_WORKSPACE_ASSETS, asset.getStorageKey())) {
            if (inputStream == null) {
                log.warn("export asset: empty stream assetId={} key={}", assetId, asset.getStorageKey());
                return null;
            }
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0) {
                log.warn("export asset: zero bytes assetId={}", assetId);
                return null;
            }
            return ResolvedExportAsset.builder()
                    .bytes(bytes)
                    .contentType(asset.getContentType())
                    .fileName(asset.getOriginalName())
                    .build();
        } catch (IOException | RuntimeException ex) {
            log.warn("export asset: storage read failed assetId={} reason={}", assetId, ex.getMessage());
            return null;
        }
    }

    private static boolean looksLikeHttpUrl(String url) {
        String lower = url.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    private Long parseAssetId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        String decoded = Parser.unescapeEntities(imageUrl.trim(), false);
        int prefixIndex = decoded.indexOf(DocumentAssetUrlHelper.ASSET_API_PREFIX);
        if (prefixIndex < 0) {
            return null;
        }
        String rest = decoded.substring(prefixIndex + DocumentAssetUrlHelper.ASSET_API_PREFIX.length());
        int end = 0;
        while (end < rest.length() && Character.isDigit(rest.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return null;
        }
        try {
            return Long.parseLong(rest.substring(0, end));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
