package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.DocumentAssetReadDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentAssetUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentAssetService {

    DocumentAssetUploadResponseDTO uploadAsset(Long nodeId, MultipartFile file);

    DocumentAssetReadDTO readAsset(Long assetId);

    /** 删除文档节点下全部资产（MinIO 对象 + 数据库记录） */
    void deleteAssetsByNodeId(Long nodeId);
}
