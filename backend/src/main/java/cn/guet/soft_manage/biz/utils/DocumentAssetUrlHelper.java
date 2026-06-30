package cn.guet.soft_manage.biz.utils;

/**
 * 文档资产 URL 处理：统一返回后端代理路径，不暴露 MinIO 地址
 */
public final class DocumentAssetUrlHelper {

    public static final String ASSET_API_PREFIX = "/api/assets/";

    private DocumentAssetUrlHelper() {
    }

    public static String toProxyUrl(Long assetId) {
        if (assetId == null) {
            return null;
        }
        return ASSET_API_PREFIX + assetId;
    }
}
