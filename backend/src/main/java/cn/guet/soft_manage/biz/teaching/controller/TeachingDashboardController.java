package cn.guet.soft_manage.biz.teaching.controller;

import cn.guet.soft_manage.biz.teaching.dto.TeachingDashboardResponseDTO;
import cn.guet.soft_manage.biz.teaching.service.TeachingDashboardService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教学工作台控制器
 */
@RestController
@RequestMapping("/api/teaching")
public class TeachingDashboardController {

    @Resource
    private TeachingDashboardService teachingDashboardService;

    @GetMapping("/dashboard")
    public Response<TeachingDashboardResponseDTO> dashboard(
            @RequestParam(required = false) Long courseId) {
        return Response.success(teachingDashboardService.getDashboard(courseId));
    }
}
