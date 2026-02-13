package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.controller.dto.CreateGroupRequestDTO;
import cn.guet.feishu.controller.dto.GroupMemberDTO;
import cn.guet.feishu.entity.*;
import cn.guet.feishu.mapper.*;
import cn.guet.feishu.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final ProjectGroupMapper projectGroupMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ProjectGroup createGroup(String userId, CreateGroupRequestDTO request) {
        Project project = projectMapper.selectByProjectId(request.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        ProjectMember projectMember = projectMemberMapper.selectByProjectIdAndUserId(request.getProjectId(), userId);
        if (projectMember == null) {
            throw new BusinessException("您不是该项目成员");
        }

        GroupMember existMember = groupMemberMapper.selectByProjectIdAndUserId(request.getProjectId(), userId);
        if (existMember != null) {
            throw new BusinessException("您已经在该项目的一个小组中");
        }

        if (project.getAllowStudentCreateGroup() == 0) {
            throw new BusinessException("该项目不允许学生创建小组");
        }

        if (project.getGroupDeadline() != null && LocalDateTime.now().isAfter(project.getGroupDeadline())) {
            throw new BusinessException("小组创建已截止");
        }

        int groupCount = projectGroupMapper.countByProjectId(request.getProjectId());
        String groupCode = String.format("GRP%03d", groupCount + 1);

        ProjectGroup group = new ProjectGroup();
        group.setGroupId(UUID.randomUUID().toString());
        group.setProjectId(request.getProjectId());
        group.setGroupName(request.getGroupName());
        group.setGroupCode(groupCode);
        group.setDescription(request.getDescription());
        group.setLeaderId(userId);
        group.setCreatorId(userId);
        group.setCurrentMembers(1);
        group.setMaxMembers(project.getMaxGroupSize());
        group.setStatus(1);

        projectGroupMapper.insert(group);

        GroupMember member = new GroupMember();
        member.setGroupMemberId(UUID.randomUUID().toString());
        member.setGroupId(group.getGroupId());
        member.setUserId(userId);
        member.setRoleInGroup("LEADER");
        member.setStatus(1);
        groupMemberMapper.insert(member);

        return group;
    }

    @Override
    public ProjectGroup getGroupById(String groupId) {
        ProjectGroup group = projectGroupMapper.selectByGroupId(groupId);
        if (group == null) {
            throw new BusinessException("小组不存在");
        }
        return group;
    }

    @Override
    public List<ProjectGroup> getProjectGroups(String projectId) {
        return projectGroupMapper.selectByProjectId(projectId);
    }

    @Override
    public ProjectGroup getMyGroup(String userId, String projectId) {
        GroupMember member = groupMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            return null;
        }
        return projectGroupMapper.selectByGroupId(member.getGroupId());
    }

    @Override
    @Transactional
    public void joinGroup(String userId, String groupId) {
        ProjectGroup group = projectGroupMapper.selectByGroupId(groupId);
        if (group == null) {
            throw new BusinessException("小组不存在");
        }

        if (group.getStatus() != 1) {
            throw new BusinessException("小组已解散");
        }

        ProjectMember projectMember = projectMemberMapper.selectByProjectIdAndUserId(group.getProjectId(), userId);
        if (projectMember == null) {
            throw new BusinessException("您不是该项目成员");
        }

        GroupMember existMember = groupMemberMapper.selectByProjectIdAndUserId(group.getProjectId(), userId);
        if (existMember != null) {
            throw new BusinessException("您已经在该项目的一个小组中");
        }

        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            throw new BusinessException("小组已满员");
        }

        GroupMember member = new GroupMember();
        member.setGroupMemberId(UUID.randomUUID().toString());
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRoleInGroup("MEMBER");
        member.setStatus(1);
        groupMemberMapper.insert(member);

        group.setCurrentMembers(group.getCurrentMembers() + 1);
        projectGroupMapper.updateByGroupId(group);
    }

    @Override
    @Transactional
    public void quitGroup(String userId, String groupId) {
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null) {
            throw new BusinessException("您不是该小组成员");
        }

        if ("LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException("组长不能直接退出，请先转让组长或解散小组");
        }

        groupMemberMapper.deleteByGroupMemberId(member.getGroupMemberId());

        ProjectGroup group = projectGroupMapper.selectByGroupId(groupId);
        group.setCurrentMembers(group.getCurrentMembers() - 1);
        projectGroupMapper.updateByGroupId(group);
    }

    @Override
    public List<GroupMemberDTO> getGroupMembers(String groupId) {
        List<GroupMember> members = groupMemberMapper.selectByGroupId(groupId);
        return members.stream()
                .map(member -> {
                    User user = userMapper.selectByUserId(member.getUserId());
                    GroupMemberDTO dto = new GroupMemberDTO();
                    dto.setGroupMemberId(member.getGroupMemberId());
                    dto.setGroupId(member.getGroupId());
                    dto.setUserId(member.getUserId());
                    dto.setRoleInGroup(member.getRoleInGroup());
                    if (user != null) {
                        dto.setUsername(user.getUsername());
                        dto.setRealName(user.getRealName());
                        dto.setStudentId(user.getStudentId());
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public void dissolveGroup(String groupId) {
        ProjectGroup group = projectGroupMapper.selectByGroupId(groupId);
        if (group == null) {
            throw new BusinessException("小组不存在");
        }

        groupMemberMapper.deleteByGroupId(groupId);

        group.setStatus(2);
        group.setCurrentMembers(0);
        projectGroupMapper.updateByGroupId(group);
    }

    @Override
    @Transactional
    public void transferLeader(String groupId, String newLeaderId) {
        ProjectGroup group = projectGroupMapper.selectByGroupId(groupId);
        if (group == null) {
            throw new BusinessException("小组不存在");
        }

        GroupMember newLeader = groupMemberMapper.selectByGroupIdAndUserId(groupId, newLeaderId);
        if (newLeader == null) {
            throw new BusinessException("新组长不是小组成员");
        }

        GroupMember oldLeader = groupMemberMapper.selectByGroupIdAndUserId(groupId, group.getLeaderId());
        if (oldLeader != null) {
            oldLeader.setRoleInGroup("MEMBER");
            groupMemberMapper.updateByGroupMemberId(oldLeader);
        }

        newLeader.setRoleInGroup("LEADER");
        groupMemberMapper.updateByGroupMemberId(newLeader);

        group.setLeaderId(newLeaderId);
        projectGroupMapper.updateByGroupId(group);
    }
}

