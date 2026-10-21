package cn.guet.soft_manage.biz.team.service;

import cn.guet.soft_manage.biz.team.dto.TeamCreateRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TeamJoinableSummaryDTO;
import cn.guet.soft_manage.biz.team.dto.TeamMemberAddRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TeamMembersResponseDTO;
import cn.guet.soft_manage.biz.team.dto.TeamOverviewSummaryDTO;
import cn.guet.soft_manage.biz.team.dto.TeamTopicSubmitRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TopicApprovalReviewRequestDTO;
import cn.guet.soft_manage.biz.team.dto.TopicApprovalSummaryDTO;
import cn.guet.soft_manage.biz.team.entity.Team;
import cn.guet.soft_manage.biz.team.entity.TeamMember;
import cn.guet.soft_manage.biz.team.entity.TopicApproval;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 组队服务
 */
public interface TeamService {

    /**
     * 创建小组
     * @param request 创建请求
     * @return 小组信息
     */
    Team createTeam(TeamCreateRequestDTO request);

    /**
     * 添加小组成员
     * @param request 添加请求
     * @return 成员信息
     */
    TeamMember addMember(TeamMemberAddRequestDTO request);

    /**
     * 当前学生加入指定小组
     */
    TeamMember joinTeam(Long teamId);

    /**
     * 当前学生可加入的同课号小组列表
     */
    List<TeamJoinableSummaryDTO> listJoinableTeamsForCurrentStudent();

    /**
     * 移除小组成员
     * @param teamId 小组ID
     * @param userId 用户ID
     */
    void removeMember(Long teamId, Long userId);

    /**
     * 查询小组成员列表
     * @param teamId 小组ID
     * @return 成员列表
     */
    TeamMembersResponseDTO listMembers(Long teamId);

    /**
     * 提交选题
     * @param request 选题请求
     * @return 小组信息
     */
    Team submitTopic(TeamTopicSubmitRequestDTO request);

    /**
     * 审批选题
     * @param request 审批请求
     */
    void reviewTopic(TopicApprovalReviewRequestDTO request);

    /**
     * 查询小组选题审批记录
     * @param teamId 小组ID
     * @return 审批记录列表
     */
    List<TopicApproval> listTopicApprovals(Long teamId);

    /**
     * 教师端选题审批收件箱
     * @param approvalStatus 审批状态筛选，可为空
     * @return 审批列表
     */
    List<TopicApprovalSummaryDTO> listTeacherTopicApprovals(String approvalStatus);

    /**
     * 教师端小组总览
     * @param teamStatus 小组状态筛选，可为空
     * @return 小组列表
     */
    List<TeamOverviewSummaryDTO> listTeacherTeamOverview(String teamStatus);

    /**
     * 获取当前用户所在小组
     * @return 小组信息
     */
    Team getCurrentTeam();
}
