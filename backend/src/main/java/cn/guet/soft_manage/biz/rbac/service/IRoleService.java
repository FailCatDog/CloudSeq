package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.entity.SysRole;

import java.util.List;

/**
 * 系统角色查询
 */
public interface IRoleService {

    SysRole getByRoleKey(String roleKey);

    List<SysRole> listActiveRolesByUserId(Long userId);

    /**
     * 优先匹配 preferredRoleKey（通常为 sm_user.role），否则取 role_sort 最小者
     */
    SysRole resolvePrimaryRole(Long userId, String preferredRoleKey);
}
