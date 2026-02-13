package cn.guet.feishu.controller;

import cn.guet.feishu.common.result.Result;
import cn.guet.feishu.controller.dto.CreateProjectRequestDTO;
import cn.guet.feishu.controller.dto.ProjectListItemDTO;
import cn.guet.feishu.controller.dto.ProjectMemberDTO;
import cn.guet.feishu.controller.dto.UpdateProjectRequestDTO;
import cn.guet.feishu.entity.Project;
import cn.guet.feishu.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public Result<Project> createProject(HttpServletRequest request, @Valid @RequestBody CreateProjectRequestDTO createRequest) {
        String userId = (String) request.getAttribute("userId");
        Project project = projectService.createProject(userId, createRequest);
        return Result.success(project);
    }

    @PutMapping("/{projectId}")
    public Result<Void> updateProject(@PathVariable String projectId, @RequestBody UpdateProjectRequestDTO updateRequest) {
        projectService.updateProject(projectId, updateRequest);
        return Result.success();
    }

    @DeleteMapping("/{projectId}")
    public Result<Void> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return Result.success();
    }

    @GetMapping("/{projectId}")
    public Result<Project> getProject(@PathVariable String projectId) {
        Project project = projectService.getProjectById(projectId);
        return Result.success(project);
    }

    @GetMapping("/code/{projectCode}")
    public Result<Project> getProjectByCode(@PathVariable String projectCode) {
        Project project = projectService.getProjectByCode(projectCode);
        return Result.success(project);
    }

    @GetMapping("/my")
    public Result<List<ProjectListItemDTO>> getMyProjects(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<ProjectListItemDTO> projects = projectService.getMyProjects(userId);
        return Result.success(projects);
    }

    @GetMapping("/all")
    public Result<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return Result.success(projects);
    }

    @PostMapping("/join")
    public Result<Void> joinProject(HttpServletRequest request, @RequestParam String projectCode) {
        String userId = (String) request.getAttribute("userId");
        projectService.joinProject(userId, projectCode);
        return Result.success();
    }

    @DeleteMapping("/{projectId}/quit")
    public Result<Void> quitProject(HttpServletRequest request, @PathVariable String projectId) {
        String userId = (String) request.getAttribute("userId");
        projectService.quitProject(userId, projectId);
        return Result.success();
    }

    @GetMapping("/{projectId}/members")
    public Result<List<ProjectMemberDTO>> getProjectMembers(@PathVariable String projectId) {
        List<ProjectMemberDTO> members = projectService.getProjectMembers(projectId);
        return Result.success(members);
    }
}

