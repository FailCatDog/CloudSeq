package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.entity.PlanTask;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 计划任务服务
 */
public interface PlanTaskService {

    PlanTask create(PlanTask task);

    PlanTask update(PlanTask task);

    void delete(Long id);

    PlanTask getById(Long id);

    List<PlanTask> listByWorkspace(Long workspaceId);
}
