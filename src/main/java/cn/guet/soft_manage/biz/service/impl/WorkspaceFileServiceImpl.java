package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WorkspaceFileDao;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceFile;
import cn.guet.soft_manage.biz.service.WorkspaceFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区 Office 文件服务实现
 */
@Service
public class WorkspaceFileServiceImpl implements WorkspaceFileService {

    @Resource
    private WorkspaceFileDao workspaceFileDao;

    @Override
    public WorkspaceFile getByNodeId(Long nodeId) {
        return null;
    }

    @Override
    public WorkspaceFile create(WorkspaceFile file) {
        return null;
    }

    @Override
    public WorkspaceFile update(WorkspaceFile file) {
        return null;
    }

    @Override
    public void deleteByNodeId(Long nodeId) {
    }
}
