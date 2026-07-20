package cn.guet.soft_manage.biz.export.dto;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单次节点导出上下文
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportContext {

    private Long nodeId;

    private Long workspaceId;

    private String nodeType;

    private String title;

    private String contentMd;

    private ExportAssetResolver assetResolver;
}
