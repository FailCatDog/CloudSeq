package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.entity.Workspace;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区服务
 */
public interface WorkspaceService {

    Workspace createWorkspace(Long teamId);

    Workspace getByTeamId(Long teamId);

    Workspace getOrCreateForCurrentTeam();
}
