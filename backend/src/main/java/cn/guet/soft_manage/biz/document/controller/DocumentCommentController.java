package cn.guet.soft_manage.biz.document.controller;

import cn.guet.soft_manage.biz.document.dto.DocumentCommentCreateRequestDTO;
import cn.guet.soft_manage.biz.document.dto.DocumentCommentDTO;
import cn.guet.soft_manage.biz.document.service.DocumentCommentService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文档评论（块级锚点；前端展示后续接入）
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentCommentController {

    @Resource
    private DocumentCommentService documentCommentService;

    @GetMapping("/{nodeId}/comments")
    public Response<List<DocumentCommentDTO>> listComments(@PathVariable Long nodeId) {
        return Response.success(documentCommentService.listByNodeId(nodeId));
    }

    @PostMapping("/{nodeId}/comments")
    public Response<DocumentCommentDTO> createComment(
            @PathVariable Long nodeId,
            @RequestBody DocumentCommentCreateRequestDTO request) {
        return Response.success(documentCommentService.create(nodeId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public Response<Void> deleteComment(@PathVariable Long commentId) {
        documentCommentService.delete(commentId);
        return Response.success();
    }
}
