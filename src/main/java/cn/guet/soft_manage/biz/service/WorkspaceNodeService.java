package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.WorkspaceNodeCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceNodeRenameRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceNodeTreeDTO;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceNode;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区节点（目录树）服务
 */
public interface WorkspaceNodeService {

    List<WorkspaceNodeTreeDTO> getTree(Long workspaceId);

    WorkspaceNode createNode(WorkspaceNodeCreateRequestDTO request);

    WorkspaceNode renameNode(Long nodeId, WorkspaceNodeRenameRequestDTO request);

    void deleteNode(Long nodeId);
}
