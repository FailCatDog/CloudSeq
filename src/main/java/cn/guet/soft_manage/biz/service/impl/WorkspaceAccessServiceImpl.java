package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.TeamDao;
import cn.guet.soft_manage.biz.dao.TeamMemberDao;
import cn.guet.soft_manage.biz.dao.UserDao;
import cn.guet.soft_manage.biz.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.pojo.entity.Team;
import cn.guet.soft_manage.biz.pojo.entity.TeamMember;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.pojo.entity.Workspace;
import cn.guet.soft_manage.biz.service.WorkspaceAccessService;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 工作区访问权限实现
 */
@Service
public class WorkspaceAccessServiceImpl implements WorkspaceAccessService {

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private TeamDao teamDao;

    @Resource
    private TeamMemberDao teamMemberDao;

    @Resource
    private UserDao userDao;

    @Override
    public WorkspaceAccessContext requireCurrentAccess(Long workspaceId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        Workspace workspace = workspaceDao.selectById(workspaceId);
        if (workspace == null) {
            throw new BusinessException(BizResponseCode.WORKSPACE_NOT_FOUND);
        }

        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }

        if (Objects.equals(user.getRole(), CacheCode.USER_ROLE_TEACHER.getCode())) {
            return WorkspaceAccessContext.builder()
                    .userId(userId)
                    .workspaceId(workspace.getId())
                    .teamId(workspace.getTeamId())
                    .readOnly(true)
                    .canWrite(false)
                    .build();
        }

        TeamMember member = teamMemberDao.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getTeamId, workspace.getTeamId())
                .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode()));
        if (member == null) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }

        Team team = teamDao.selectById(workspace.getTeamId());
        if (team == null) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }

        boolean unlocked = Objects.equals(team.getStatus(), CacheCode.TEAM_STATUS_UNLOCKED.getCode());
        return WorkspaceAccessContext.builder()
                .userId(userId)
                .workspaceId(workspace.getId())
                .teamId(workspace.getTeamId())
                .readOnly(false)
                .canWrite(unlocked)
                .build();
    }

    @Override
    public Workspace requireWritableWorkspace(Long workspaceId) {
        WorkspaceAccessContext context = requireCurrentAccess(workspaceId);
        if (!context.isCanWrite()) {
            if (context.isReadOnly()) {
                throw new BusinessException(BizResponseCode.TEACHER_READ_ONLY);
            }
            throw new BusinessException(BizResponseCode.WORKSPACE_LOCKED);
        }

        Workspace workspace = workspaceDao.selectById(workspaceId);
        if (workspace == null) {
            throw new BusinessException(BizResponseCode.WORKSPACE_NOT_FOUND);
        }
        return workspace;
    }
}
