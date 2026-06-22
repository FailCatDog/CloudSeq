package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.entity.WorkspaceFile;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区 Office 文件服务
 */
public interface WorkspaceFileService {

    WorkspaceFile getByNodeId(Long nodeId);

    WorkspaceFile create(WorkspaceFile file);

    WorkspaceFile update(WorkspaceFile file);

    void deleteByNodeId(Long nodeId);
}
