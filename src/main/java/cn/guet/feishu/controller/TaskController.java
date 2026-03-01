package cn.guet.feishu.controller;

import cn.guet.feishu.common.result.Result;
import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.ProjectTask;
import cn.guet.feishu.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Result<ProjectTask> createTask(HttpServletRequest request, 
                                          @Valid @RequestBody CreateTaskRequestDTO dto) {
        String userId = (String) request.getAttribute("userId");
        ProjectTask task = taskService.createTask(userId, dto);
        return Result.success(task);
    }

    @PutMapping("/{taskId}")
    public Result<Void> updateTask(HttpServletRequest request,
                                    @PathVariable String taskId,
                                    @RequestBody UpdateTaskRequestDTO dto) {
        String userId = (String) request.getAttribute("userId");
        taskService.updateTask(userId, taskId, dto);
        return Result.success();
    }

    @DeleteMapping("/{taskId}")
    public Result<Void> deleteTask(HttpServletRequest request,
                                    @PathVariable String taskId) {
        String userId = (String) request.getAttribute("userId");
        taskService.deleteTask(userId, taskId);
        return Result.success();
    }

    @GetMapping("/{taskId}")
    public Result<TaskDetailDTO> getTaskDetail(@PathVariable String taskId) {
        TaskDetailDTO task = taskService.getTaskDetail(taskId);
        return Result.success(task);
    }

    @GetMapping("/project/{projectId}")
    public Result<List<ProjectTask>> getProjectTasks(@PathVariable String projectId,
                                                      @RequestParam(required = false) Integer status,
                                                      @RequestParam(required = false) Integer priority) {
        List<ProjectTask> tasks = taskService.getProjectTasks(projectId, status, priority);
        return Result.success(tasks);
    }

    @GetMapping("/group/{groupId}")
    public Result<List<ProjectTask>> getGroupTasks(@PathVariable String groupId) {
        List<ProjectTask> tasks = taskService.getGroupTasks(groupId);
        return Result.success(tasks);
    }

    @GetMapping("/my")
    public Result<List<ProjectTask>> getMyTasks(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<ProjectTask> tasks = taskService.getMyTasks(userId);
        return Result.success(tasks);
    }

    @PostMapping("/{taskId}/assign")
    public Result<Void> assignTask(HttpServletRequest request,
                                    @PathVariable String taskId,
                                    @RequestParam String userIds) {
        String userId = (String) request.getAttribute("userId");
        List<String> userIdList = List.of(userIds.split(","));
        taskService.assignTask(userId, taskId, userIdList);
        return Result.success();
    }

    @PutMapping("/{taskId}/assignment")
    public Result<Void> updateAssignment(HttpServletRequest request,
                                          @PathVariable String taskId,
                                          @RequestBody UpdateAssignmentRequestDTO dto) {
        String userId = (String) request.getAttribute("userId");
        taskService.updateAssignment(userId, taskId, dto);
        return Result.success();
    }

    @PostMapping("/{taskId}/dependency")
    public Result<Void> addDependency(HttpServletRequest request,
                                       @PathVariable String taskId,
                                       @RequestParam String dependOnTaskId) {
        String userId = (String) request.getAttribute("userId");
        taskService.addDependency(userId, taskId, dependOnTaskId);
        return Result.success();
    }

    @DeleteMapping("/{taskId}/dependency")
    public Result<Void> removeDependency(HttpServletRequest request,
                                          @PathVariable String taskId,
                                          @RequestParam String dependOnTaskId) {
        String userId = (String) request.getAttribute("userId");
        taskService.removeDependency(userId, taskId, dependOnTaskId);
        return Result.success();
    }
}

