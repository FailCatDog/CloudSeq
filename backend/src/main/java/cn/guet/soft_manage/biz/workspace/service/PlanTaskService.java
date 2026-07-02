package cn.guet.soft_manage.biz.workspace.service;

import cn.guet.soft_manage.biz.workspace.entity.PlanTask;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 计划任务服务
 */
public interface PlanTaskService {

    /**
     * 创建计划任务
     * @param task 任务信息
     * @return 任务信息
     */
    PlanTask create(PlanTask task);

    /**
     * 更新计划任务
     * @param task 任务信息
     * @return 任务信息
     */
    PlanTask update(PlanTask task);

    /**
     * 删除计划任务
     * @param id 任务ID
     */
    void delete(Long id);

    /**
     * 按ID查询计划任务
     * @param id 任务ID
     * @return 任务信息
     */
    PlanTask getById(Long id);

    /**
     * 查询工作区下全部计划任务
     * @param workspaceId 工作区ID
     * @return 任务列表
     */
    List<PlanTask> listByWorkspace(Long workspaceId);
}
