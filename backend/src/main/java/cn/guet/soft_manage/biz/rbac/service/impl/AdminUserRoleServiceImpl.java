package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dao.SysRoleDao;
import cn.guet.soft_manage.biz.rbac.dao.SysUserRoleDao;
import cn.guet.soft_manage.biz.rbac.dto.response.AdminUserSummaryDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.RoleSummaryDTO;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.entity.SysUserRole;
import cn.guet.soft_manage.biz.rbac.service.IAdminUserRoleService;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import cn.guet.soft_manage.biz.rbac.support.AdminAuthSupport;
import cn.guet.soft_manage.biz.user.dao.UserDao;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminUserRoleServiceImpl implements IAdminUserRoleService {

    @Resource
    private UserDao userDao;

    @Resource
    private SysUserRoleDao sysUserRoleDao;

    @Resource
    private SysRoleDao sysRoleDao;

    @Resource
    private IRoleService roleService;

    @Resource
    private AdminAuthSupport adminAuthSupport;

    @Override
    public List<AdminUserSummaryDTO> search(String keyword, String roleKey) {
        adminAuthSupport.requireAdmin();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .orderByDesc(User::getCreateDate)
                .orderByDesc(User::getId);
        if (StringUtils.hasText(keyword)) {
            String q = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, q)
                    .or().like(User::getRealName, q)
                    .or().like(User::getNickName, q)
                    .or().like(User::getStudentNo, q));
        }
        List<User> users = userDao.selectList(wrapper.last("LIMIT 200"));
        return users.stream()
                .map(user -> toSummary(user, roleService.listActiveRolesByUserId(user.getId())))
                .filter(summary -> matchRoleKey(summary, roleKey))
                .toList();
    }

    @Override
    public AdminUserSummaryDTO getUserRoles(Long userId) {
        adminAuthSupport.requireAdmin();
        User user = requireUser(userId);
        return toSummary(user, roleService.listActiveRolesByUserId(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceUserRoles(Long userId, List<Long> roleIds) {
        adminAuthSupport.requireAdmin();
        requireUser(userId);
        if (roleIds != null) {
            for (Long roleId : new LinkedHashSet<>(roleIds)) {
                if (roleId != null && sysRoleDao.selectById(roleId) == null) {
                    throw new BusinessException(BizResponseCode.ROLE_NOT_FOUND);
                }
            }
        }
        sysUserRoleDao.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(roleIds);
        for (Long roleId : uniqueIds) {
            if (roleId == null) {
                continue;
            }
            sysUserRoleDao.insert(SysUserRole.builder()
                    .userId(userId)
                    .roleId(roleId)
                    .build());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer isActive) {
        adminAuthSupport.requireAdmin();
        if (isActive == null || (isActive != 0 && isActive != 1)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        User user = requireUser(userId);
        Long currentUserId = UserContext.getUserId();
        if (Objects.equals(currentUserId, userId) && isActive == 0) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        user.setIsActive(isActive);
        userDao.updateById(user);
    }

    private User requireUser(Long userId) {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new BusinessException(BizResponseCode.USER_NOT_FOUND);
        }
        return user;
    }

    private boolean matchRoleKey(AdminUserSummaryDTO summary, String roleKey) {
        if (!StringUtils.hasText(roleKey)) {
            return true;
        }
        if (summary.getRoles() == null || summary.getRoles().isEmpty()) {
            return false;
        }
        return summary.getRoles().stream()
                .anyMatch(role -> Objects.equals(role.getRoleKey(), roleKey.trim()));
    }

    private AdminUserSummaryDTO toSummary(User user, List<SysRole> roles) {
        List<RoleSummaryDTO> roleSummaries = roles == null ? Collections.emptyList() : roles.stream()
                .map(role -> RoleSummaryDTO.builder()
                        .id(role.getId())
                        .roleKey(role.getRoleKey())
                        .roleName(role.getRoleName())
                        .roleSort(role.getRoleSort())
                        .dataScope(role.getDataScope())
                        .homePath(role.getHomePath())
                        .status(role.getStatus())
                        .remark(role.getRemark())
                        .version(role.getVersion())
                        .build())
                .toList();
        return AdminUserSummaryDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .nickName(user.getNickName())
                .studentNo(user.getStudentNo())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .roles(roleSummaries)
                .build();
    }
}
