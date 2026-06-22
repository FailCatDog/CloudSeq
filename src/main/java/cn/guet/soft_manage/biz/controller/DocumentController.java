package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentDetailDTO;
import cn.guet.soft_manage.biz.service.DocumentService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: Markdown 文档控制器
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
