package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.OfficeCallbackRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeCallbackResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeDocumentDetailDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeEditorConfigDTO;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceFile;

/**
 * OnlyOffice 文档集成服务
 */
public interface OfficeDocumentService {

    OfficeDocumentDetailDTO getOfficeDocument(Long nodeId);

    OfficeEditorConfigDTO getEditorConfig(Long nodeId);

    org.springframework.core.io.Resource downloadDocument(Long nodeId, String token);

    OfficeCallbackResponseDTO handleCallback(Long nodeId, OfficeCallbackRequestDTO request);
}
