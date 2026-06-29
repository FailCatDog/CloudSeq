package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.TeamCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMemberAddRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMembersResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamTopicSubmitRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TopicApprovalReviewRequestDTO;
import cn.guet.soft_manage.biz.pojo.entity.Team;
import cn.guet.soft_manage.biz.pojo.entity.TeamMember;
import cn.guet.soft_manage.biz.pojo.entity.TopicApproval;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 组队服务
 */
public interface TeamService {

    Team createTeam(TeamCreateRequestDTO request);

    TeamMember addMember(TeamMemberAddRequestDTO request);

    void removeMember(Long teamId, Long userId);

    TeamMembersResponseDTO listMembers(Long teamId);

    Team submitTopic(TeamTopicSubmitRequestDTO request);

    void reviewTopic(TopicApprovalReviewRequestDTO request);

    List<TopicApproval> listTopicApprovals(Long teamId);

    Team getCurrentTeam();
}
