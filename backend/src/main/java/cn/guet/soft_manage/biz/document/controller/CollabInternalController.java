package cn.guet.soft_manage.biz.document.controller;

import cn.guet.soft_manage.biz.document.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistResponseDTO;
import cn.guet.soft_manage.biz.document.service.DocumentService;
import cn.guet.soft_manage.biz.sheet.service.SheetService;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.frame.common.Response;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 协同服务内部 API 控制器
 */
@RestController
@RequestMapping("/api/internal/collab")
public class CollabInternalController {

    @Resource
    private DocumentService documentService;

    @Resource
    private SheetService sheetService;

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @GetMapping("/load/{nodeId}")
    public Response<CollabLoadResponseDTO> load(@PathVariable Long nodeId) {
        WorkspaceNode node = requireCollabNode(nodeId);
        if (isDocumentNode(node)) {
            return Response.success(documentService.loadForCollab(nodeId));
        }
        return Response.success(sheetService.loadForCollab(nodeId));
    }

    @PostMapping("/persist")
    public Response<CollabPersistResponseDTO> persist(@RequestBody CollabPersistRequestDTO request) {
        if (request == null || request.getNodeId() == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        WorkspaceNode node = requireCollabNode(request.getNodeId());
        if (isDocumentNode(node)) {
            return Response.success(documentService.persistFromCollab(request));
        }
        return Response.success(sheetService.persistFromCollab(request));
    }

    private WorkspaceNode requireCollabNode(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!isDocumentNode(node) && !isSheetNode(node)) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);
        }
        return node;
    }

    private boolean isDocumentNode(WorkspaceNode node) {
        return Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode());
    }

    private boolean isSheetNode(WorkspaceNode node) {
        return Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode());
    }
}
