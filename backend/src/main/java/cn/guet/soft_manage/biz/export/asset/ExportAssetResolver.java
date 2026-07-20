package cn.guet.soft_manage.biz.export.asset;

import cn.guet.soft_manage.biz.export.dto.ResolvedExportAsset;

/**
 * 解析 Markdown 内嵌图片 URL 为可嵌入导出的字节资产
 */
public interface ExportAssetResolver {

    ResolvedExportAsset resolve(Long workspaceId, String imageUrl);
}
