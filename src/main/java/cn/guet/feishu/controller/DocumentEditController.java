package cn.guet.feishu.controller;

import cn.guet.feishu.common.result.Result;
import cn.guet.feishu.entity.DocumentEdit;
import cn.guet.feishu.service.DocumentEditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document/edit")
@RequiredArgsConstructor
@Slf4j
public class DocumentEditController {

    private final DocumentEditService documentEditService;

    /**
     * 获取文档编辑内容
     */
    @GetMapping("/{documentId}")
    public Result<DocumentEdit> getDocumentEdit(@PathVariable String documentId) {
        DocumentEdit edit = documentEditService.getDocumentEdit(documentId);
        return Result.success(edit);
    }

    /**
     * 开始编辑文档
     */
    @PostMapping("/{documentId}/start")
    public Result<DocumentEdit> startEdit(HttpServletRequest request,
                                          @PathVariable String documentId) {
        String userId = (String) request.getAttribute("userId");
        DocumentEdit edit = documentEditService.startEdit(userId, documentId);
        return Result.success(edit);
    }

    /**
     * 保存文档内容
     */
    @PostMapping("/{documentId}/save")
    public Result<Void> saveContent(@PathVariable String documentId,
                                     @RequestBody String content) {
        documentEditService.saveContent(documentId, content);
        return Result.success();
    }

    /**
     * 释放编辑锁
     */
    @PostMapping("/{documentId}/release")
    public Result<Void> releaseEdit(HttpServletRequest request,
                                     @PathVariable String documentId) {
        String userId = (String) request.getAttribute("userId");
        documentEditService.releaseEdit(userId, documentId);
        return Result.success();
    }

    /**
     * 编辑心跳
     */
    @PostMapping("/{documentId}/heartbeat")
    public Result<Void> heartbeat(HttpServletRequest request,
                                   @PathVariable String documentId) {
        String userId = (String) request.getAttribute("userId");
        documentEditService.heartbeat(userId, documentId);
        return Result.success();
    }
}

