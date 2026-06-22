package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.entity.WeeklyReport;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-09
 * @Description: 周报服务
 */
public interface WeeklyReportService {

    WeeklyReport create(WeeklyReport report);

    WeeklyReport update(WeeklyReport report);

    WeeklyReport submit(Long id);

    void delete(Long id);

    WeeklyReport getById(Long id);

    WeeklyReport getByWeek(Long workspaceId, Integer reportYear, Integer reportWeek);

    List<WeeklyReport> listMine(Long workspaceId);
}
