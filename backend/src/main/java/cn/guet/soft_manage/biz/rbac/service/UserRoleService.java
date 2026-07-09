package cn.guet.soft_manage.biz.rbac.service;

/**
 * 用户-角色绑定
 */
public interface UserRoleService {

    void assignRole(Long userId, String roleKey);
}
