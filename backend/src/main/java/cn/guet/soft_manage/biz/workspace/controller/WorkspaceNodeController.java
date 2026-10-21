package cn.guet.soft_manage.biz.workspace.controller;

import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeCreateRequestDTO;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeRenameRequestDTO;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeTreeDTO;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceNodeService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区目录树控制器
 */
@RestController
@RequestMapping("/api/workspace/nodes")
public class WorkspaceNodeController {

    @Resource
    private WorkspaceNodeService workspaceNodeService;

    @GetMapping("/tree")
    public Response<List<WorkspaceNodeTreeDTO>> getTree(@RequestParam Long workspaceId) {
        return Response.success(workspaceNodeService.getTree(workspaceId));
    }

    @PostMapping
    public Response<WorkspaceNode> create(@RequestBody WorkspaceNodeCreateRequestDTO request) {
        return Response.success(workspaceNodeService.createNode(request));
    }

    @PutMapping("/{nodeId}/rename")
    public Response<WorkspaceNode> rename(
            @PathVariable Long nodeId,
            @RequestBody WorkspaceNodeRenameRequestDTO request) {
        return Response.success(workspaceNodeService.renameNode(nodeId, request));
    }

    @DeleteMapping("/{nodeId}")
    public Response<Void> delete(@PathVariable Long nodeId) {
        workspaceNodeService.deleteNode(nodeId);
        return Response.success();
    }
}
