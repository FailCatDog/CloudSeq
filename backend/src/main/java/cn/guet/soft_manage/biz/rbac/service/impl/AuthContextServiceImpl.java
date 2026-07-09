package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dto.AuthContextDTO;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.service.AuthContextService;
import cn.guet.soft_manage.biz.rbac.service.DataScopeService;
import cn.guet.soft_manage.biz.rbac.service.MenuService;
import cn.guet.soft_manage.biz.rbac.service.PermissionService;
import cn.guet.soft_manage.biz.rbac.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthContextServiceImpl implements AuthContextService {

    @Resource
    private PermissionService permissionService;

    @Resource
    private MenuService menuService;

    @Resource
    private RoleService roleService;

    @Resource
    private DataScopeService dataScopeService;

    @Override
    public AuthContextDTO build(Long userId) {
        return build(userId, null);
    }

    @Override
    public AuthContextDTO build(Long userId, String preferredRoleKey) {
        if (userId == null) {
            return AuthContextDTO.builder().build();
        }
        SysRole primaryRole = roleService.resolvePrimaryRole(userId, preferredRoleKey);
        String home = primaryRole != null && StringUtils.hasText(primaryRole.getHomePath())
                ? primaryRole.getHomePath()
                : null;
        String dataScope = primaryRole != null && StringUtils.hasText(primaryRole.getDataScope())
                ? primaryRole.getDataScope()
                : dataScopeService.resolveDataScope(userId);

        return AuthContextDTO.builder()
                .permissions(permissionService.getPermissionCodes(userId))
                .menus(menuService.buildMenuTree(userId))
                .home(home)
                .dataScope(dataScope)
                .build();
    }
}
