package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.TeamDao;
import cn.guet.soft_manage.biz.dao.TeamMemberDao;
import cn.guet.soft_manage.biz.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.pojo.entity.Team;
import cn.guet.soft_manage.biz.pojo.entity.TeamMember;
import cn.guet.soft_manage.biz.pojo.entity.Workspace;
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
import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区服务实现
 */
@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private TeamMemberDao teamMemberDao;

    @Resource
    private TeamDao teamDao;

    @Override
    public Workspace getCurrentTeamWorkspace() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        TeamMember member = teamMemberDao.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getMemberStatus, CacheCode.MEMBER_STATUS_ACTIVE.getCode()));
        if (member == null) {
            throw new BusinessException(BizResponseCode.NOT_IN_TEAM);
        }

        Team team = teamDao.selectById(member.getTeamId());
        if (team == null) {
            throw new BusinessException(BizResponseCode.NOT_IN_TEAM);
        }

        Workspace workspace = getByTeamId(team.getId());
        if (workspace == null) {
            if (!Objects.equals(team.getStatus(), CacheCode.TEAM_STATUS_UNLOCKED.getCode())) {
                throw new BusinessException(BizResponseCode.WORKSPACE_LOCKED);
            }
            throw new BusinessException(BizResponseCode.WORKSPACE_NOT_FOUND);
        }
        return workspace;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Workspace createWorkspace(Long teamId) {
        Workspace existing = getByTeamId(teamId);
        if (Objects.nonNull(existing)) {
            return existing;
        }

        Long userId = UserContext.getUserId();
        Workspace workspace = Workspace.builder()
                .teamId(teamId)
                .unlockAt(LocalDateTime.now())
                .createUser(userId)
                .updateUser(userId)
                .build();
        workspaceDao.insert(workspace);
        return workspace;
    }

    @Override
    public Workspace getByTeamId(Long teamId) {
        return workspaceDao.selectOne(new LambdaQueryWrapper<Workspace>()
                .eq(Workspace::getTeamId, teamId));
    }
}
