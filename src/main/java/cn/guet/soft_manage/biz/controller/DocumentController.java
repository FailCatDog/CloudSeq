package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentDetailDTO;
import cn.guet.soft_manage.biz.service.DocumentService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档控制器（协同编辑）
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Resource
    private DocumentService documentService;

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
}
