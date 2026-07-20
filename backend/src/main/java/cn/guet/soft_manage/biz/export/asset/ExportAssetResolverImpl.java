package cn.guet.soft_manage.biz.export.asset;

import cn.guet.soft_manage.biz.document.dao.WorkspaceAssetDao;
import cn.guet.soft_manage.biz.document.entity.WorkspaceAsset;
import cn.guet.soft_manage.biz.document.util.DocumentAssetUrlHelper;
import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;
import cn.guet.soft_manage.frame.constant.MinioConstants;
import cn.guet.soft_manage.frame.storage.ObjectStorageService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class ExportAssetResolverImpl implements ExportAssetResolver {

    private final WorkspaceAssetDao assetDao;
    private final ObjectStorageService storage;

    public ExportAssetResolverImpl(WorkspaceAssetDao assetDao, ObjectStorageService storage) {
        this.assetDao = assetDao;
        this.storage = storage;
    }

    @Override
    public ResolvedExportAsset resolve(Long workspaceId, String imageUrl) {
        if (workspaceId == null) {
            return null;
        }
        Long assetId = parseAssetId(imageUrl);
        if (assetId == null) {
            return null;
        }

        WorkspaceAsset asset = assetDao.selectById(assetId);
        if (asset == null || !workspaceId.equals(asset.getWorkspaceId())) {
            return null;
        }

        try (InputStream inputStream = storage.getObject(
                MinioConstants.BUCKET_WORKSPACE_ASSETS, asset.getStorageKey())) {
            if (inputStream == null) {
                return null;
            }
            byte[] bytes = inputStream.readAllBytes();
            return ResolvedExportAsset.builder()
                    .bytes(bytes)
                    .contentType(asset.getContentType())
                    .fileName(asset.getOriginalName())
                    .build();
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private Long parseAssetId(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        int prefixIndex = imageUrl.indexOf(DocumentAssetUrlHelper.ASSET_API_PREFIX);
        if (prefixIndex < 0) {
            return null;
        }
        String rest = imageUrl.substring(prefixIndex + DocumentAssetUrlHelper.ASSET_API_PREFIX.length());
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
