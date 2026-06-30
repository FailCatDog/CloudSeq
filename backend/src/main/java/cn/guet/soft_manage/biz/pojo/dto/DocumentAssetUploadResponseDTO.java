package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档资产上传响应（url 为后端代理地址，非 MinIO 直链）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAssetUploadResponseDTO {

    private Long assetId;

    private Long nodeId;

    private Long workspaceId;

    /** 后端代理读取地址，如 /api/assets/{assetId} */
    private String url;

    private String fileName;

    private String contentType;

    private Long size;

    private String assetType;
}
