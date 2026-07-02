package cn.guet.soft_manage.biz.workspace.service;

import cn.guet.soft_manage.biz.workspace.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.workspace.entity.Workspace;

/**
 * 工作区访问权限
 */
public interface WorkspaceAccessService {

    /**
     * 校验并获取当前用户对工作区的访问上下文
     * @param workspaceId 工作区ID
     * @return 访问上下文
     */
    WorkspaceAccessContext requireCurrentAccess(Long workspaceId);

    /**
     * 校验当前用户对工作区具有写权限
     * @param workspaceId 工作区ID
     * @return 工作区信息
     */
    Workspace requireWritableWorkspace(Long workspaceId);
}
