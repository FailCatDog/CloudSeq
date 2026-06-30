package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.TeamCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMemberAddRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMembersResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamTopicSubmitRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TopicApprovalReviewRequestDTO;
import cn.guet.soft_manage.biz.pojo.entity.Team;
import cn.guet.soft_manage.biz.pojo.entity.TeamMember;
import cn.guet.soft_manage.biz.pojo.entity.TopicApproval;
import cn.guet.soft_manage.biz.service.TeamService;
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

    @GetMapping("/{teamId}/topic-approvals")
    public Response<List<TopicApproval>> listTopicApprovals(@PathVariable Long teamId) {
        return Response.success(teamService.listTopicApprovals(teamId));
    }

    @GetMapping("/current")
    public Response<Team> getCurrentTeam() {
        return Response.success(teamService.getCurrentTeam());
    }
}
