package cn.guet.soft_manage.biz.workspace.service;

import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeCreateRequestDTO;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeRenameRequestDTO;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeTreeDTO;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区节点（目录树）服务
 */
public interface WorkspaceNodeService {

    /**
     * 获取工作区节点树
     * @param workspaceId 工作区ID
     * @return 节点树
     */
    List<WorkspaceNodeTreeDTO> getTree(Long workspaceId);

    /**
     * 创建工作区节点
     * @param request 创建请求
     * @return 节点信息
     */
    WorkspaceNode createNode(WorkspaceNodeCreateRequestDTO request);

    /**
     * 重命名工作区节点
     * @param nodeId 节点ID
     * @param request 重命名请求
     * @return 节点信息
     */
    WorkspaceNode renameNode(Long nodeId, WorkspaceNodeRenameRequestDTO request);

    /**
     * 删除工作区节点
     * @param nodeId 节点ID
     */
    void deleteNode(Long nodeId);
}
