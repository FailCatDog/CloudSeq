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

    /**
     * 创建项目（教师）
     */
    @PostMapping
    public Result<Project> createProject(HttpServletRequest request, @Valid @RequestBody CreateProjectRequestDTO createRequest) {
        String userId = (String) request.getAttribute("userId");
        Project project = projectService.createProject(userId, createRequest);
        return Result.success(project);
    }

    /**
     * 更新项目信息
     */
    @PutMapping("/{projectId}")
    public Result<Void> updateProject(@PathVariable String projectId, @RequestBody UpdateProjectRequestDTO updateRequest) {
        projectService.updateProject(projectId, updateRequest);
        return Result.success();
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{projectId}")
    public Result<Void> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return Result.success();
    }

    /**
     * 根据ID获取项目详情
     */
    @GetMapping("/{projectId}")
    public Result<Project> getProject(@PathVariable String projectId) {
        Project project = projectService.getProjectById(projectId);
        return Result.success(project);
    }

    /**
     * 根据项目编码获取项目详情
     */
    @GetMapping("/code/{projectCode}")
    public Result<Project> getProjectByCode(@PathVariable String projectCode) {
        Project project = projectService.getProjectByCode(projectCode);
        return Result.success(project);
    }

    /**
     * 获取我参与的项目列表
     */
    @GetMapping("/my")
    public Result<List<ProjectListItemDTO>> getMyProjects(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<ProjectListItemDTO> projects = projectService.getMyProjects(userId);
        return Result.success(projects);
    }

    /**
     * 获取所有项目列表
     */
    @GetMapping("/all")
    public Result<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return Result.success(projects);
    }

    /**
     * 通过项目编码加入项目
     */
    @PostMapping("/join")
    public Result<Void> joinProject(HttpServletRequest request, @RequestParam String projectCode) {
        String userId = (String) request.getAttribute("userId");
        projectService.joinProject(userId, projectCode);
        return Result.success();
    }

    /**
     * 邀请用户加入项目
     */
    @PostMapping("/{projectId}/invite")
    public Result<Void> inviteProjectMember(HttpServletRequest request,
                                             @PathVariable String projectId,
                                             @RequestParam String targetUserId) {
        String userId = (String) request.getAttribute("userId");
        projectService.inviteProjectMember(userId, projectId, targetUserId);
        return Result.success();
    }

    /**
     * 退出项目
     */
    @DeleteMapping("/{projectId}/quit")
    public Result<Void> quitProject(HttpServletRequest request, @PathVariable String projectId) {
        String userId = (String) request.getAttribute("userId");
        projectService.quitProject(userId, projectId);
        return Result.success();
    }

    /**
     * 获取项目成员列表
     */
    @GetMapping("/{projectId}/members")
    public Result<List<ProjectMemberDTO>> getProjectMembers(@PathVariable String projectId) {
        List<ProjectMemberDTO> members = projectService.getProjectMembers(projectId);
        return Result.success(members);
    }
}

