package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.TeamDao;
import cn.guet.soft_manage.biz.dao.TeamMemberDao;
import cn.guet.soft_manage.biz.dao.TopicApprovalDao;
import cn.guet.soft_manage.biz.dao.UserDao;
import cn.guet.soft_manage.biz.pojo.dto.TeamCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMemberAddRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMemberInfoDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamMembersResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.TeamTopicSubmitRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.TopicApprovalReviewRequestDTO;
import cn.guet.soft_manage.biz.pojo.entity.Team;
import cn.guet.soft_manage.biz.pojo.entity.TeamMember;
import cn.guet.soft_manage.biz.pojo.entity.TopicApproval;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.service.TeamService;
import cn.guet.soft_manage.biz.service.WorkspaceService;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 组队服务实现
 */
@Service
public class TeamServiceImpl implements TeamService {

    @Resource
    private TeamDao teamDao;

    @Resource
    private TeamMemberDao teamMemberDao;

    @Resource
    private TopicApprovalDao topicApprovalDao;

    @Resource
    private UserDao userDao;

    @Resource
    private WorkspaceService workspaceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Team createTeam(TeamCreateRequestDTO request) {
        User leader = userDao.selectById(request.getLeaderUserId());
        if (Objects.isNull(leader)) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        boolean exists = teamDao.exists(new LambdaQueryWrapper<Team>()
                .eq(Team::getLeaderUserId, request.getLeaderUserId()));
        if (exists) {
            throw new BusinessException(BizResponseCode.TEAM_LEADER_ALREADY_ASSIGNED);
        }

        Team team = Team.builder()
                .teamName(request.getTeamName())
                .leaderUserId(request.getLeaderUserId())
                .status(CacheCode.TEAM_STATUS_NORMAL.getCode())
                .createUser(UserContext.getUserId())
                .updateUser(UserContext.getUserId())
                .build();
        teamDao.insert(team);

        TeamMember leaderMember = TeamMember.builder()
                .teamId(team.getId())
                .userId(request.getLeaderUserId())
                .isLeader(1)
                .memberStatus(CacheCode.MEMBER_STATUS_ACTIVE.getCode())
                .joinDate(LocalDateTime.now())
                .createUser(UserContext.getUserId())
                .updateUser(UserContext.getUserId())
                .build();
        teamMemberDao.insert(leaderMember);
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TeamMember addMember(TeamMemberAddRequestDTO request) {
        Team team = teamDao.selectById(request.getTeamId());
        if (Objects.isNull(team)) {
            throw new BusinessException(BizResponseCode.TEAM_NOT_FOUND);
        }
        boolean inTeam = teamMemberDao.exists(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, request.getUserId())
                .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode()));
        if (inTeam) {
            throw new BusinessException(BizResponseCode.TEAM_MEMBER_ALREADY_EXISTS);
        }

        TeamMember member = TeamMember.builder()
                .teamId(request.getTeamId())
                .userId(request.getUserId())
                .isLeader(0)
                .memberStatus(CacheCode.MEMBER_STATUS_ACTIVE.getCode())
                .joinDate(LocalDateTime.now())
                .createUser(UserContext.getUserId())
                .updateUser(UserContext.getUserId())
                .build();
        teamMemberDao.insert(member);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long teamId, Long userId) {
        TeamMember member = teamMemberDao.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        if (Objects.isNull(member)) {
            throw new BusinessException(BizResponseCode.TEAM_MEMBER_NOT_FOUND);
        }
        if (Objects.equals(member.getIsLeader(), 1)) {
            throw new BusinessException(BizResponseCode.TEAM_LEADER_CANNOT_QUIT);
        }
        member.setMemberStatus(CacheCode.MEMBER_STATUS_LEFT.getCode());
        member.setLeftDate(LocalDateTime.now());
        teamMemberDao.updateById(member);
    }

    @Override
    public TeamMembersResponseDTO listMembers(Long teamId) {
        List<TeamMember> members = teamMemberDao.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode())
                .orderByDesc(TeamMember::getIsLeader)
                .orderByAsc(TeamMember::getJoinDate)
                .orderByAsc(TeamMember::getId));

        return TeamMembersResponseDTO.builder()
                .members(members)
                .memberList(buildMemberInfoList(members))
                .build();
    }

    private List<TeamMemberInfoDTO> buildMemberInfoList(List<TeamMember> members) {
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = members.stream()
                .map(TeamMember::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = userDao.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (left, right) -> left));

        return members.stream()
                .map(member -> toMemberInfo(member, userMap.get(member.getUserId())))
                .toList();
    }

    private TeamMemberInfoDTO toMemberInfo(TeamMember member, User user) {
        return TeamMemberInfoDTO.builder()
                .teamMemberId(member.getId())
                .userId(member.getUserId())
                .name(resolveDisplayName(user, member.getUserId()))
                .username(user != null ? user.getUsername() : null)
                .studentNo(user != null ? user.getStudentNo() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .isLeader(member.getIsLeader())
                .memberStatus(member.getMemberStatus())
                .joinDate(member.getJoinDate())
                .build();
    }

    private String resolveDisplayName(User user, Long userId) {
        if (user == null) {
            return userId == null ? "未知成员" : "用户" + userId;
        }
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName().trim();
        }
        if (user.getNickName() != null && !user.getNickName().isBlank()) {
            return user.getNickName().trim();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        return "用户" + user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Team submitTopic(TeamTopicSubmitRequestDTO request) {
        Team team = teamDao.selectById(request.getTeamId());
        if (Objects.isNull(team)) {
            throw new BusinessException(BizResponseCode.TEAM_NOT_FOUND);
        }
        team.setTopicTitle(request.getTopicTitle());
        team.setTopicDesc(request.getTopicDesc());
        team.setStatus(CacheCode.TEAM_STATUS_PENDING_TOPIC.getCode());
        teamDao.updateById(team);

        TopicApproval approval = TopicApproval.builder()
                .teamId(request.getTeamId())
                .topicTitle(request.getTopicTitle())
                .topicDesc(request.getTopicDesc())
                .approvalStatus(CacheCode.APPROVAL_STATUS_PENDING.getCode())
                .submitUserId(team.getLeaderUserId())
                .submitDate(LocalDateTime.now())
                .createUser(UserContext.getUserId())
                .updateUser(UserContext.getUserId())
                .build();
        topicApprovalDao.insert(approval);
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewTopic(TopicApprovalReviewRequestDTO request) {
        TopicApproval approval = topicApprovalDao.selectById(request.getApprovalId());
        if (Objects.isNull(approval)) {
            throw new BusinessException(BizResponseCode.APPROVAL_NOT_FOUND);
        }

        approval.setApprovalStatus(request.getApprovalStatus());
        approval.setRejectReason(request.getRejectReason());
        approval.setApproveUserId(UserContext.getUserId());
        approval.setApproveDate(LocalDateTime.now());
        approval.setUpdateUser(UserContext.getUserId());
        topicApprovalDao.updateById(approval);

        Team team = teamDao.selectById(approval.getTeamId());
        if (team == null) {
            return;
        }

        if (Objects.equals(request.getApprovalStatus(), CacheCode.APPROVAL_STATUS_APPROVED.getCode())) {
            team.setStatus(CacheCode.TEAM_STATUS_UNLOCKED.getCode());
            team.setUpdateUser(UserContext.getUserId());
            teamDao.updateById(team);
            workspaceService.createWorkspace(team.getId());
        } else if (Objects.equals(request.getApprovalStatus(), CacheCode.APPROVAL_STATUS_REJECTED.getCode())) {
            team.setStatus(CacheCode.TEAM_STATUS_TOPIC_REJECTED.getCode());
            team.setUpdateUser(UserContext.getUserId());
            teamDao.updateById(team);
        }
    }

    @Override
    public List<TopicApproval> listTopicApprovals(Long teamId) {
        return topicApprovalDao.selectList(new LambdaQueryWrapper<TopicApproval>()
                .eq(TopicApproval::getTeamId, teamId)
                .orderByDesc(TopicApproval::getSubmitDate));
    }

    @Override
    public Team getCurrentTeam() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        TeamMember member = teamMemberDao.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode()));
        if (Objects.isNull(member)) {
            return null;
        }

        Team team = teamDao.selectById(member.getTeamId());
        if (Objects.isNull(team)) {
            return null;
        }
        return team;
    }
}
