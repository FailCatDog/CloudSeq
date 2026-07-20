package cn.guet.soft_manage.biz.user.service.impl;

import cn.guet.soft_manage.biz.course.dao.CourseDao;
import cn.guet.soft_manage.biz.course.dao.CourseEnrollmentDao;
import cn.guet.soft_manage.biz.course.entity.Course;
import cn.guet.soft_manage.biz.course.entity.CourseEnrollment;
import cn.guet.soft_manage.biz.team.dao.TeamDao;
import cn.guet.soft_manage.biz.team.dao.TeamMemberDao;
import cn.guet.soft_manage.biz.team.dao.TopicApprovalDao;
import cn.guet.soft_manage.biz.team.entity.Team;
import cn.guet.soft_manage.biz.team.entity.TeamMember;
import cn.guet.soft_manage.biz.team.entity.TopicApproval;
import cn.guet.soft_manage.biz.rbac.dto.AuthContextDTO;
import cn.guet.soft_manage.biz.rbac.service.IAuthContextService;
import cn.guet.soft_manage.biz.rbac.service.IUserRoleService;
import cn.guet.soft_manage.biz.user.dao.UserDao;
import cn.guet.soft_manage.biz.user.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.user.dto.LoginResponseDTO;
import cn.guet.soft_manage.biz.user.dto.ProfileStatusAggregate;
import cn.guet.soft_manage.biz.user.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.biz.user.param.UserParam;
import cn.guet.soft_manage.biz.user.service.UserService;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.workspace.entity.Workspace;
import cn.guet.soft_manage.frame.auth.JwtUtil;
import cn.guet.soft_manage.frame.auth.LoginUser;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private CourseEnrollmentDao courseEnrollmentDao;

    @Resource
    private CourseDao courseDao;

    @Resource
    private TeamMemberDao teamMemberDao;

    @Resource
    private TeamDao teamDao;

    @Resource
    private TopicApprovalDao topicApprovalDao;

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private IAuthContextService authContextService;

    @Resource
    private IUserRoleService userRoleService;

    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userDao.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getIsActive, 1));
        if (Objects.isNull(user)) {
            throw new BusinessException(BizResponseCode.LOGIN_FAILED);
        }
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(BizResponseCode.LOGIN_FAILED);
        }

        user.setLastLoginAt(LocalDateTime.now());
        userDao.updateById(user);

        LoginUser loginUser = LoginUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .studentNo(user.getStudentNo())
                .nickName(user.getNickName())
                .realName(user.getRealName())
                .avatarUrl(user.getAvatarUrl())
                .build();
        String token = JwtUtil.generateToken(loginUser);
        AuthContextDTO authContext = authContextService.build(user.getId(), user.getRole());
        return LoginResponseDTO.builder()
                .user(user)
                .authorization(token)
                .permissions(authContext.getPermissions())
                .menus(authContext.getMenus())
                .home(authContext.getHome())
                .dataScope(authContext.getDataScope())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequestDTO request) {
        if (CacheCode.USER_ROLE_STUDENT.getCode().equals(request.getRole()) && (Objects.isNull(request.getStudentNo()) || request.getStudentNo().isBlank())) {
            throw new BusinessException(BizResponseCode.STUDENT_NO_REQUIRED);
        }

        boolean exists = userDao.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (exists) {
            throw new BusinessException(BizResponseCode.USERNAME_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setRole(CacheCode.USER_ROLE_STUDENT.getCode());
        user.setCreateUser(1L);
        user.setUpdateUser(1L);
        userDao.insert(user);
        userRoleService.assignRole(user.getId(), CacheCode.USER_ROLE_STUDENT.getCode());
    }

    @Override
    public AuthContextDTO getAuthContext() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        return authContextService.build(userId, user.getRole());
    }

    @Override
    public User getProfile(Long userId) {
        User user = userDao.selectById(userId);
        if (Objects.isNull(user)) throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(UserParam param) {
        User user = userDao.selectById(UserContext.getUserId());
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        user.setNickName(param.getNickName());
        user.setRealName(param.getRealName());
        user.setBio(param.getBio());
        user.setAvatarUrl(param.getAvatarUrl());
        userDao.updateById(user);
    }

    @Override
    public ProfileStatusAggregate getProfileStatus() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }

        ProfileStatusAggregate.ProfileStatusAggregateBuilder builder = ProfileStatusAggregate.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .nickName(user.getNickName())
                .studentNo(user.getStudentNo())
                .role(user.getRole());

        if (!Objects.equals(user.getRole(), CacheCode.USER_ROLE_STUDENT.getCode())) {
            return builder.build();
        }

        CourseEnrollment enrollment = courseEnrollmentDao.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getUserId, userId)
                .eq(CourseEnrollment::getEnrollStatus, CacheCode.ENROLL_STATUS_ENROLLED.getCode())
                .orderByDesc(CourseEnrollment::getEnrollDate)
                .orderByDesc(CourseEnrollment::getId)
                .last("LIMIT 1"));

        Course course = null;
        if (enrollment != null) {
            course = courseDao.selectById(enrollment.getCourseId());
            builder.enrollmentId(enrollment.getId())
                    .courseId(enrollment.getCourseId())
                    .studentNo(enrollment.getStudentNo() != null ? enrollment.getStudentNo() : user.getStudentNo())
                    .enrollStatus(enrollment.getEnrollStatus())
                    .enrollDate(enrollment.getEnrollDate());
            if (course != null) {
                builder.courseCode(course.getCourseCode())
                        .courseName(course.getCourseName())
                        .termYear(course.getTermYear())
                        .termSeason(course.getTermSeason());
            }
        }

        TeamMember member = teamMemberDao.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode())
                .orderByDesc(TeamMember::getJoinDate)
                .orderByDesc(TeamMember::getId)
                .last("LIMIT 1"));

        Team team = null;
        if (member != null) {
            team = teamDao.selectById(member.getTeamId());
            if (team != null) {
                Long memberCount = teamMemberDao.selectCount(new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, team.getId())
                        .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode()));

                builder.teamId(team.getId())
                        .teamName(team.getTeamName())
                        .teamCourseId(team.getCourseId())
                        .leaderUserId(team.getLeaderUserId())
                        .teamStatus(team.getStatus())
                        .topicTitle(team.getTopicTitle())
                        .topicDesc(team.getTopicDesc())
                        .isLeader(member.getIsLeader())
                        .memberCount(memberCount != null ? memberCount.intValue() : 0)
                        .teamCreateDate(team.getCreateDate());

                TopicApproval latestApproval = topicApprovalDao.selectOne(new LambdaQueryWrapper<TopicApproval>()
                        .eq(TopicApproval::getTeamId, team.getId())
                        .orderByDesc(TopicApproval::getSubmitDate)
                        .orderByDesc(TopicApproval::getId)
                        .last("LIMIT 1"));
                if (latestApproval != null) {
                    builder.latestApprovalId(latestApproval.getId())
                            .approvalStatus(latestApproval.getApprovalStatus())
                            .rejectReason(latestApproval.getRejectReason())
                            .approvalSubmitDate(latestApproval.getSubmitDate());
                }

                Workspace workspace = workspaceDao.selectOne(new LambdaQueryWrapper<Workspace>()
                        .eq(Workspace::getTeamId, team.getId())
                        .last("LIMIT 1"));
                if (workspace != null) {
                    builder.workspaceId(workspace.getId())
                            .workspaceUnlockAt(workspace.getUnlockAt());
                }
            }
        }

        builder.milestoneStatus(resolveMilestoneStatus(enrollment, team));
        return builder.build();
    }

    private String resolveMilestoneStatus(CourseEnrollment enrollment, Team team) {
        if (team != null) {
            String teamStatus = team.getStatus();
            if (Objects.equals(teamStatus, CacheCode.TEAM_STATUS_UNLOCKED.getCode())) {
                return CacheCode.STUDENT_MILESTONE_READY.getCode();
            }
            if (Objects.equals(teamStatus, CacheCode.TEAM_STATUS_PENDING_TOPIC.getCode())) {
                return CacheCode.STUDENT_MILESTONE_TOPIC_PENDING.getCode();
            }
            if (Objects.equals(teamStatus, CacheCode.TEAM_STATUS_TOPIC_REJECTED.getCode())) {
                return CacheCode.STUDENT_MILESTONE_TOPIC_REJECTED.getCode();
            }
            return CacheCode.STUDENT_MILESTONE_NEED_TOPIC.getCode();
        }
        if (enrollment != null) {
            return CacheCode.STUDENT_MILESTONE_NEED_TEAM.getCode();
        }
        return CacheCode.STUDENT_MILESTONE_NEED_ENROLL.getCode();
    }
}
