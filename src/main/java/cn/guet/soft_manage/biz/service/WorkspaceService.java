package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.entity.Workspace;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区服务
 */
public interface WorkspaceService {

    /**
     * 获取当前用户所在小组的项目空间（仅查询，不在此创建）。
     * 项目空间在选题审批通过时由 {@link cn.guet.soft_manage.biz.service.impl.TeamServiceImpl#reviewTopic} 自动创建。
     */
    Workspace getCurrentTeamWorkspace();

    Workspace createWorkspace(Long teamId);

    Workspace getByTeamId(Long teamId);
}
