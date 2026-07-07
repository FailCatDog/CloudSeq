package cn.guet.soft_manage.biz.team.controller;

import cn.guet.soft_manage.biz.team.dto.TeamCreateRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TeamMemberAddRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TeamMembersResponseDTO;
import cn.guet.soft_manage.biz.team.dto.TeamTopicSubmitRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TopicApprovalReviewRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TopicApprovalSummaryDTO;
import cn.guet.soft_manage.biz.team.entity.Team;
import cn.guet.soft_manage.biz.team.entity.TeamMember;
import cn.guet.soft_manage.biz.team.entity.TopicApproval;
import cn.guet.soft_manage.biz.team.service.TeamService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 组队控制器
 */
@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Resource
    private TeamService teamService;

    @PostMapping
    public Response<Team> create(@Valid @RequestBody TeamCreateRequestDTO request) {
        return Response.success(teamService.createTeam(request));
    }

    @PostMapping("/member")
    public Response<TeamMember> addMember(@Valid @RequestBody TeamMemberAddRequestDTO request) {
        return Response.success(teamService.addMember(request));
    }

    @DeleteMapping("/member")
    public Response<Void> removeMember(@RequestParam Long teamId, @RequestParam Long userId) {
        teamService.removeMember(teamId, userId);
        return Response.success();
    }

    @GetMapping("/{teamId}/members")
    public Response<TeamMembersResponseDTO> listMembers(@PathVariable Long teamId) {
        return Response.success(teamService.listMembers(teamId));
    }

    @PostMapping("/topic/submit")
    public Response<Team> submitTopic(@Valid @RequestBody TeamTopicSubmitRequestDTO request) {
        return Response.success(teamService.submitTopic(request));
    }

    @PostMapping("/topic/review")
    public Response<Void> reviewTopic(@Valid @RequestBody TopicApprovalReviewRequestDTO request) {
        teamService.reviewTopic(request);
        return Response.success();
    }

    @GetMapping("/topic-approvals")
    public Response<List<TopicApprovalSummaryDTO>> listTeacherTopicApprovals(
            @RequestParam(required = false) String approvalStatus) {
        return Response.success(teamService.listTeacherTopicApprovals(approvalStatus));
    }

    @GetMapping("/{teamId}/topic-approvals")
    public Response<List<TopicApproval>> listTopicApprovals(@PathVariable Long teamId) {
        return Response.success(teamService.listTopicApprovals(teamId));
    }

    @GetMapping("/current")
    public Response<Team> getCurrentTeam() {
        return Response.success(teamService.getCurrentTeam());
    }
}
