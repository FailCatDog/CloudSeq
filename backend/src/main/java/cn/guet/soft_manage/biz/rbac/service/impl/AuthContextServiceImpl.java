package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dto.AuthContextDTO;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.service.IAuthContextService;
import cn.guet.soft_manage.biz.rbac.service.IDataScopeService;
import cn.guet.soft_manage.biz.rbac.service.IMenuService;
import cn.guet.soft_manage.biz.rbac.service.IPermissionService;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthContextServiceImpl implements IAuthContextService {

    @Resource
    private IPermissionService permissionService;

    @Resource
    private IMenuService menuService;

    @Resource
    private IRoleService roleService;

    @Resource
    private IDataScopeService dataScopeService;

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
