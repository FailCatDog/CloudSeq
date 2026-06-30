package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.DocumentAssetUploadResponseDTO;
import cn.guet.soft_manage.biz.service.DocumentAssetService;
import cn.guet.soft_manage.frame.common.Response;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档资产上传（后端代理写入 MinIO）
 */
@RestController
@RequestMapping("/api/documents")
@ConditionalOnBean(DocumentAssetService.class)
public class DocumentAssetController {

    @Resource
    private DocumentAssetService documentAssetService;

    @PostMapping("/{nodeId}/assets")
    public Response<DocumentAssetUploadResponseDTO> uploadAsset(
            @PathVariable Long nodeId,
            @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BizResponseCode.FILE_EMPTY);
        }
        return Response.success(documentAssetService.uploadAsset(nodeId, file));
    }
}
