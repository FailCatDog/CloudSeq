package cn.guet.soft_manage.biz.workspace.service;

import cn.guet.soft_manage.biz.workspace.dto.TeacherWeeklyReviewResponseDTO;
import cn.guet.soft_manage.biz.workspace.entity.WeeklyReport;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-09
 * @Description: 周报服务
 */
public interface WeeklyReportService {

    /**
     * 创建周报
     * @param report 周报信息
     * @return 周报信息
     */
    WeeklyReport create(WeeklyReport report);

    /**
     * 更新周报
     * @param report 周报信息
     * @return 周报信息
     */
    WeeklyReport update(WeeklyReport report);

    /**
     * 提交周报
     * @param id 周报ID
     * @return 周报信息
     */
    WeeklyReport submit(Long id);

    /**
     * 删除周报
     * @param id 周报ID
     */
    void delete(Long id);

    /**
     * 按ID查询周报
     * @param id 周报ID
     * @return 周报信息
     */
    WeeklyReport getById(Long id);

    /**
     * 按周查询周报
     * @param workspaceId 工作区ID
     * @param reportYear 报告年份
     * @param reportWeek 报告周次
     * @return 周报信息
     */
    WeeklyReport getByWeek(Long workspaceId, Integer reportYear, Integer reportWeek);

    /**
     * 查询当前用户在工作区下的周报列表
     * @param workspaceId 工作区ID
     * @return 周报列表
     */
    List<WeeklyReport> listMine(Long workspaceId);

    /**
     * 教师端周报审阅
     * @param courseId 课号 ID
     * @param reportYear 报告年份，可为空
     * @param reportWeek 报告周次，可为空
     * @return 审阅数据
     */
    TeacherWeeklyReviewResponseDTO getTeacherWeeklyReview(Long courseId, Integer reportYear, Integer reportWeek);
}
