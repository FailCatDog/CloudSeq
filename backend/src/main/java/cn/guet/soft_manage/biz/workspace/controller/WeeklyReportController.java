package cn.guet.soft_manage.biz.workspace.controller;

import cn.guet.soft_manage.biz.workspace.dto.TeacherWeeklyReviewResponseDTO;
import cn.guet.soft_manage.biz.workspace.entity.WeeklyReport;
import cn.guet.soft_manage.biz.workspace.service.WeeklyReportService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-09
 * @Description: 周报控制器
 */
@RestController
@RequestMapping("/api/weekly-report")
public class WeeklyReportController {

    @Resource
    private WeeklyReportService weeklyReportService;

    @PostMapping
    public Response<WeeklyReport> create(@RequestBody WeeklyReport report) {
        return Response.success(weeklyReportService.create(report));
    }

    @PutMapping
    public Response<WeeklyReport> update(@RequestBody WeeklyReport report) {
        return Response.success(weeklyReportService.update(report));
    }

    @PostMapping("/{id}/submit")
    public Response<WeeklyReport> submit(@PathVariable Long id) {
        return Response.success(weeklyReportService.submit(id));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        weeklyReportService.delete(id);
        return Response.success();
    }

    @GetMapping("/teacher/review")
    public Response<TeacherWeeklyReviewResponseDTO> teacherReview(
            @RequestParam Long courseId,
            @RequestParam(required = false) Integer reportYear,
            @RequestParam(required = false) Integer reportWeek) {
        return Response.success(weeklyReportService.getTeacherWeeklyReview(courseId, reportYear, reportWeek));
    }

    @GetMapping("/{id}")
    public Response<WeeklyReport> getById(@PathVariable Long id) {
        return Response.success(weeklyReportService.getById(id));
    }

    @GetMapping("/workspace/{workspaceId}")
    public Response<WeeklyReport> getByWeek(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Integer reportYear,
            @RequestParam(required = false) Integer reportWeek) {
        return Response.success(weeklyReportService.getByWeek(workspaceId, reportYear, reportWeek));
    }

    @GetMapping("/workspace/{workspaceId}/mine")
    public Response<List<WeeklyReport>> listMine(@PathVariable Long workspaceId) {
        return Response.success(weeklyReportService.listMine(workspaceId));
    }
}
