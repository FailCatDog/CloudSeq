package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.controller.dto.CreateProjectRequestDTO;
import cn.guet.feishu.controller.dto.ProjectListItemDTO;
import cn.guet.feishu.controller.dto.ProjectMemberDTO;
import cn.guet.feishu.controller.dto.UpdateProjectRequestDTO;
import cn.guet.feishu.entity.Project;
import cn.guet.feishu.entity.ProjectMember;
import cn.guet.feishu.entity.User;
import cn.guet.feishu.mapper.ProjectMapper;
import cn.guet.feishu.mapper.ProjectMemberMapper;
import cn.guet.feishu.mapper.UserMapper;
import cn.guet.feishu.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Project createProject(String creatorId, CreateProjectRequestDTO request) {
        User creator = userMapper.selectByUserId(creatorId);
        if (creator == null) {
            throw new BusinessException("用户不存在");
        }

        Project project = new Project();
        project.setProjectId(UUID.randomUUID().toString());
        project.setProjectName(request.getProjectName());
        project.setProjectCode(generateProjectCode());
        project.setDescription(request.getDescription());
        project.setCreatorId(creatorId);
        project.setStartTime(request.getStartTime());
        project.setEndTime(request.getEndTime());
        project.setStatus(1);
        project.setMaxGroupSize(request.getMaxGroupSize() != null ? request.getMaxGroupSize() : 6);
        project.setMinGroupSize(request.getMinGroupSize() != null ? request.getMinGroupSize() : 3);
        project.setGroupDeadline(request.getGroupDeadline());
        project.setAllowStudentCreateGroup(request.getAllowStudentCreateGroup() != null ? request.getAllowStudentCreateGroup() : 1);

        projectMapper.insert(project);

        ProjectMember member = new ProjectMember();
        member.setProjectMemberId(UUID.randomUUID().toString());
        member.setProjectId(project.getProjectId());
        member.setUserId(creatorId);
        member.setRoleInProject("OWNER");
        member.setStatus(1);
        projectMemberMapper.insert(member);

        return project;
    }

    @Override
    public void updateProject(String projectId, UpdateProjectRequestDTO request) {
        Project existProject = projectMapper.selectByProjectId(projectId);
        if (existProject == null) {
            throw new BusinessException("项目不存在");
        }

        Project project = new Project();
        project.setProjectId(projectId);
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setStartTime(request.getStartTime());
        project.setEndTime(request.getEndTime());
        project.setStatus(request.getStatus());
        project.setMaxGroupSize(request.getMaxGroupSize());
        project.setMinGroupSize(request.getMinGroupSize());
        project.setGroupDeadline(request.getGroupDeadline());
        project.setAllowStudentCreateGroup(request.getAllowStudentCreateGroup());

        projectMapper.updateByProjectId(project);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId) {
        Project project = projectMapper.selectByProjectId(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        projectMapper.deleteByProjectId(projectId);
    }

    @Override
    public Project getProjectById(String projectId) {
        Project project = projectMapper.selectByProjectId(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    @Override
    public Project getProjectByCode(String projectCode) {
        Project project = projectMapper.selectByProjectCode(projectCode);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        return project;
    }

    @Override
    public List<ProjectListItemDTO> getMyProjects(String userId) {
        List<ProjectMember> members = projectMemberMapper.selectByUserId(userId);
        return members.stream()
                .map(member -> {
                    Project project = projectMapper.selectByProjectId(member.getProjectId());
                    ProjectListItemDTO dto = new ProjectListItemDTO();
                    dto.setId(project.getId());
                    dto.setProjectId(project.getProjectId());
                    dto.setProjectName(project.getProjectName());
                    dto.setProjectCode(project.getProjectCode());
                    return dto;
                })
                .toList();
    }

    @Override
    public List<Project> getAllProjects() {
        return projectMapper.selectAll();
    }

    @Override
    @Transactional
    public void joinProject(String userId, String projectCode) {
        Project project = projectMapper.selectByProjectCode(projectCode);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        if (project.getStatus() == 3 || project.getStatus() == 4) {
            throw new BusinessException("项目已结束或已取消，无法加入");
        }

        ProjectMember existMember = projectMemberMapper.selectByProjectIdAndUserId(project.getProjectId(), userId);
        if (existMember != null) {
            throw new BusinessException("您已经是该项目成员");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectMemberId(UUID.randomUUID().toString());
        member.setProjectId(project.getProjectId());
        member.setUserId(userId);
        member.setRoleInProject("MEMBER");
        member.setStatus(1);
        member.setInvitedBy(project.getCreatorId());

        projectMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void inviteProjectMember(String inviterId, String projectId, String targetUserId) {
        Project project = projectMapper.selectByProjectId(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        ProjectMember inviter = projectMemberMapper.selectByProjectIdAndUserId(projectId, inviterId);
        if (inviter == null || !"OWNER".equals(inviter.getRoleInProject())) {
            throw new BusinessException("只有项目创建者可以邀请成员");
        }

        ProjectMember existMember = projectMemberMapper.selectByProjectIdAndUserId(projectId, targetUserId);
        if (existMember != null) {
            throw new BusinessException("该用户已经是项目成员");
        }

        User targetUser = userMapper.selectByUserId(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("被邀请用户不存在");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectMemberId(UUID.randomUUID().toString());
        member.setProjectId(projectId);
        member.setUserId(targetUserId);
        member.setRoleInProject("MEMBER");
        member.setStatus(1);
        member.setInvitedBy(inviterId);
        projectMemberMapper.insert(member);
    }

    @Override
    public void quitProject(String userId, String projectId) {
        ProjectMember member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw new BusinessException("您不是该项目成员");
        }

        if ("OWNER".equals(member.getRoleInProject())) {
            throw new BusinessException("项目创建者不能退出项目");
        }

        projectMemberMapper.deleteByProjectMemberId(member.getProjectMemberId());
    }

    @Override
    public List<ProjectMemberDTO> getProjectMembers(String projectId) {
        List<ProjectMember> members = projectMemberMapper.selectByProjectId(projectId);
        return members.stream()
                .map(member -> {
                    User user = userMapper.selectByUserId(member.getUserId());
                    ProjectMemberDTO dto = new ProjectMemberDTO();
                    dto.setProjectMemberId(member.getProjectMemberId());
                    dto.setProjectId(member.getProjectId());
                    dto.setUserId(member.getUserId());
                    dto.setRoleInProject(member.getRoleInProject());
                    dto.setJoinTime(member.getJoinTime());
                    dto.setStatus(member.getStatus());
                    if (user != null) {
                        dto.setUsername(user.getUsername());
                        dto.setRealName(user.getRealName());
                        dto.setStudentId(user.getStudentId());
                    }
                    return dto;
                })
                .toList();
    }

    private String generateProjectCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 3).toUpperCase();
        return "PRJ" + date + uuid;
    }
}

