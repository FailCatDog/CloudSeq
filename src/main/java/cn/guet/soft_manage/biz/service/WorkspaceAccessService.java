package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.pojo.entity.Workspace;

/**
 * 工作区访问权限
 */
public interface WorkspaceAccessService {

    WorkspaceAccessContext requireCurrentAccess(Long workspaceId);

    Workspace requireWritableWorkspace(Long workspaceId);
}
