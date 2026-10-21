package cn.guet.soft_manage.biz.teaching.service;

import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardResponseDTO;

/**
 * 教学工作台服务
 */
public interface TeachingDashboardService {

    /**
     * 获取教学工作台数据
     * @param courseId 课号 ID，可为空（聚合教师全部课号）
     * @return 工作台数据
     */
    TeachingDashboardResponseDTO getDashboard(Long courseId);
}
