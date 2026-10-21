package cn.guet.soft_manage.biz.rbac.service;

/**
 * 用户-角色绑定
 */
public interface IUserRoleService {

    void assignRole(Long userId, String roleKey);
}
