package cn.guet.soft_manage.biz.workspace.controller;

import cn.guet.soft_manage.biz.workspace.entity.PlanTask;
import cn.guet.soft_manage.biz.workspace.service.PlanTaskService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 计划任务控制器
 */
@RestController
@RequestMapping("/api/plan-task")
public class PlanTaskController {

    @Resource
    private PlanTaskService planTaskService;

    @PostMapping
    public Response<PlanTask> create(@Valid @RequestBody PlanTask task) {
        return Response.success(planTaskService.create(task));
    }

    @PutMapping
    public Response<PlanTask> update(@Valid @RequestBody PlanTask task) {
        return Response.success(planTaskService.update(task));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        planTaskService.delete(id);
        return Response.success();
    }

    @GetMapping("/{id}")
    public Response<PlanTask> getById(@PathVariable Long id) {
        return Response.success(planTaskService.getById(id));
    }

    @GetMapping("/workspace/{workspaceId}")
    public Response<List<PlanTask>> listByWorkspace(@PathVariable Long workspaceId) {
        return Response.success(planTaskService.listByWorkspace(workspaceId));
    }
}
