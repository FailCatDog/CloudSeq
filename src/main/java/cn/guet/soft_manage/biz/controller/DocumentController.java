package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentDetailDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeCallbackRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeCallbackResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeDocumentDetailDTO;
import cn.guet.soft_manage.biz.pojo.dto.OfficeEditorConfigDTO;
import cn.guet.soft_manage.biz.service.DocumentService;
import cn.guet.soft_manage.biz.service.OfficeDocumentService;
import cn.guet.soft_manage.frame.common.Response;
import cn.guet.soft_manage.frame.constant.OfficeFileConstants;
import jakarta.annotation.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档控制器（Markdown + OnlyOffice）
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Resource
    private DocumentService documentService;

    @Resource
    private OfficeDocumentService officeDocumentService;

    @GetMapping("/{nodeId}")
    public Response<DocumentDetailDTO> getDocument(@PathVariable Long nodeId) {
        return Response.success(documentService.getDocument(nodeId));
    }

    @PostMapping("/{nodeId}/collab-token")
    public Response<CollabTokenResponseDTO> issueCollabToken(@PathVariable Long nodeId) {
        return Response.success(documentService.issueCollabToken(nodeId));
    }

    @PutMapping("/{nodeId}/content")
    public Response<DocumentDetailDTO> saveContent(
            @PathVariable Long nodeId,
            @RequestBody DocumentContentSaveRequestDTO request) {
        return Response.success(documentService.saveContent(nodeId, request));
    }

    @GetMapping("/{nodeId}/office")
    public Response<OfficeDocumentDetailDTO> getOfficeDocument(@PathVariable Long nodeId) {
        return Response.success(officeDocumentService.getOfficeDocument(nodeId));
    }

    @GetMapping("/{nodeId}/editor-config")
    public Response<OfficeEditorConfigDTO> getEditorConfig(@PathVariable Long nodeId) {
        return Response.success(officeDocumentService.getEditorConfig(nodeId));
    }

    @GetMapping("/{nodeId}/office/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadOfficeDocument(
            @PathVariable Long nodeId,
            @RequestParam String token) {
        org.springframework.core.io.Resource body = officeDocumentService.downloadDocument(nodeId, token);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(OfficeFileConstants.DOCX_MIME))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"document.docx\"")
                .body(body);
    }

    @PostMapping("/{nodeId}/office/callback")
    public OfficeCallbackResponseDTO handleOfficeCallback(
            @PathVariable Long nodeId,
            @RequestBody(required = false) OfficeCallbackRequestDTO request) {
        try {
            return officeDocumentService.handleCallback(nodeId, request);
        } catch (Exception ex) {
            return OfficeCallbackResponseDTO.failure();
        }
    }
}
