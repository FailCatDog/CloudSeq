package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.response.AdminUserSummaryDTO;

import java.util.List;

public interface IAdminUserRoleService {

    List<AdminUserSummaryDTO> search(String keyword, String roleKey);

    AdminUserSummaryDTO getUserRoles(Long userId);

    void replaceUserRoles(Long userId, List<Long> roleIds);

    void updateUserStatus(Long userId, Integer isActive);
}
