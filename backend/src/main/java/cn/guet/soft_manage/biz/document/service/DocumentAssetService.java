package cn.guet.soft_manage.biz.document.service;

import cn.guet.soft_manage.biz.document.dto.DocumentAssetReadDTO;
import cn.guet.soft_manage.biz.document.dto.DocumentAssetUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档资产服务
 */
public interface DocumentAssetService {

    /**
     * 上传文档资产
     * @param nodeId 节点ID
     * @param file 上传文件
     * @return 上传结果
     */
    DocumentAssetUploadResponseDTO uploadAsset(Long nodeId, MultipartFile file);

    /**
     * 读取文档资产
     * @param assetId 资产ID
     * @return 资产读取数据
     */
    DocumentAssetReadDTO readAsset(Long assetId);

    /**
     * 删除文档节点下全部资产
     * @param nodeId 节点ID
     */
    void deleteAssetsByNodeId(Long nodeId);
}
