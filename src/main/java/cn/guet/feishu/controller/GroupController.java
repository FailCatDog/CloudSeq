package cn.guet.feishu.controller;

import cn.guet.feishu.common.result.Result;
import cn.guet.feishu.controller.dto.CreateGroupRequestDTO;
import cn.guet.feishu.controller.dto.GroupMemberDTO;
import cn.guet.feishu.entity.ProjectGroup;
import cn.guet.feishu.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public Result<ProjectGroup> createGroup(HttpServletRequest request, @Valid @RequestBody CreateGroupRequestDTO createRequest) {
        String userId = (String) request.getAttribute("userId");
        ProjectGroup group = groupService.createGroup(userId, createRequest);
        return Result.success(group);
    }

    @GetMapping("/{groupId}")
    public Result<ProjectGroup> getGroup(@PathVariable String groupId) {
        ProjectGroup group = groupService.getGroupById(groupId);
        return Result.success(group);
    }

    @GetMapping("/project/{projectId}")
    public Result<List<ProjectGroup>> getProjectGroups(@PathVariable String projectId) {
        List<ProjectGroup> groups = groupService.getProjectGroups(projectId);
        return Result.success(groups);
    }

    @GetMapping("/my/{projectId}")
    public Result<ProjectGroup> getMyGroup(HttpServletRequest request, @PathVariable String projectId) {
        String userId = (String) request.getAttribute("userId");
        ProjectGroup group = groupService.getMyGroup(userId, projectId);
        return Result.success(group);
    }

    @PostMapping("/{groupId}/join")
    public Result<Void> joinGroup(HttpServletRequest request, @PathVariable String groupId) {
        String userId = (String) request.getAttribute("userId");
        groupService.joinGroup(userId, groupId);
        return Result.success();
    }

    @DeleteMapping("/{groupId}/quit")
    public Result<Void> quitGroup(HttpServletRequest request, @PathVariable String groupId) {
        String userId = (String) request.getAttribute("userId");
        groupService.quitGroup(userId, groupId);
        return Result.success();
    }

    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberDTO>> getGroupMembers(@PathVariable String groupId) {
        List<GroupMemberDTO> members = groupService.getGroupMembers(groupId);
        return Result.success(members);
    }

    @DeleteMapping("/{groupId}/dissolve")
    public Result<Void> dissolveGroup(@PathVariable String groupId) {
        groupService.dissolveGroup(groupId);
        return Result.success();
    }

    @PutMapping("/{groupId}/leader")
    public Result<Void> transferLeader(@PathVariable String groupId, @RequestParam String newLeaderId) {
        groupService.transferLeader(groupId, newLeaderId);
        return Result.success();
    }
}

