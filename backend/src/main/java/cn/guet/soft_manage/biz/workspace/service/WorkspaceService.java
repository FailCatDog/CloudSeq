package cn.guet.soft_manage.biz.workspace.service;

import cn.guet.soft_manage.biz.workspace.entity.Workspace;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区服务
 */
public interface WorkspaceService {

    /**
     * 获取当前用户所在小组的项目空间
     * @return 工作区信息
     */
    Workspace getCurrentTeamWorkspace();

    /**
     * 为小组创建项目空间
     * @param teamId 小组ID
     * @return 工作区信息
     */
    Workspace createWorkspace(Long teamId);

    /**
     * 按小组ID查询项目空间
     * @param teamId 小组ID
     * @return 工作区信息
     */
    Workspace getByTeamId(Long teamId);
}
