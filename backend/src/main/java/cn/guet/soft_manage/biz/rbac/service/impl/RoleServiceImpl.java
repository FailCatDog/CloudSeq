package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dao.SysRoleDao;
import cn.guet.soft_manage.biz.rbac.dao.SysUserRoleDao;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.entity.SysUserRole;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import cn.guet.soft_manage.frame.enums.CacheCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class RoleServiceImpl implements IRoleService {

    @Resource
    private SysRoleDao sysRoleDao;

    @Resource
    private SysUserRoleDao sysUserRoleDao;

    @Override
    public SysRole getByRoleKey(String roleKey) {
        if (!StringUtils.hasText(roleKey)) {
            return null;
        }
        return sysRoleDao.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey.trim())
                .eq(SysRole::getStatus, CacheCode.SYS_STATUS_ACTIVE.getCode())
                .last("LIMIT 1"));
    }

    @Override
    public List<SysRole> listActiveRolesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SysUserRole> userRoles = sysUserRoleDao.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        return sysRoleDao.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, CacheCode.SYS_STATUS_ACTIVE.getCode())
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getId));
    }

    @Override
    public SysRole resolvePrimaryRole(Long userId, String preferredRoleKey) {
        List<SysRole> roles = listActiveRolesByUserId(userId);
        if (roles.isEmpty()) {
            return null;
        }
        if (StringUtils.hasText(preferredRoleKey)) {
            for (SysRole role : roles) {
                if (Objects.equals(role.getRoleKey(), preferredRoleKey.trim())) {
                    return role;
                }
            }
        }
        return roles.stream()
                .min(Comparator.comparing(SysRole::getRoleSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysRole::getId))
                .orElse(roles.get(0));
    }
}
