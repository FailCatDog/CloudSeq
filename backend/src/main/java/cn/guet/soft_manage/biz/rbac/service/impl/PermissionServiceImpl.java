package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dao.SysMenuDao;
import cn.guet.soft_manage.biz.rbac.dao.SysRoleMenuDao;
import cn.guet.soft_manage.biz.rbac.entity.SysMenu;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.entity.SysRoleMenu;
import cn.guet.soft_manage.biz.rbac.service.IPermissionService;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import cn.guet.soft_manage.frame.enums.CacheCode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements IPermissionService {

    @Resource
    private IRoleService roleService;

    @Resource
    private SysRoleMenuDao sysRoleMenuDao;

    @Resource
    private SysMenuDao sysMenuDao;

    @Override
    public Set<String> getPermissionCodes(Long userId) {
        return listAuthorizedMenus(userId).stream()
                .map(SysMenu::getPerms)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public List<SysMenu> listAuthorizedMenus(Long userId) {
        List<Long> menuIds = listAuthorizedMenuIds(userId);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysMenuDao.selectList(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .eq(SysMenu::getStatus, CacheCode.SYS_STATUS_ACTIVE.getCode())
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
    }

    @Override
    public List<SysMenu> listApiMenus(Long userId) {
        return listAuthorizedMenus(userId).stream()
                .filter(menu -> StringUtils.hasText(menu.getApiPath()))
                .filter(menu -> StringUtils.hasText(menu.getApiMethod()))
                .sorted(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId))
                .toList();
    }

    @Override
    public List<SysMenu> listGlobalApiMenuRules() {
        return sysMenuDao.selectList(new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, CacheCode.SYS_STATUS_ACTIVE.getCode())
                        .eq(SysMenu::getMenuType, CacheCode.MENU_TYPE_BUTTON.getCode())
                        .isNotNull(SysMenu::getApiPath)
                        .ne(SysMenu::getApiPath, "")
                        .isNotNull(SysMenu::getApiMethod)
                        .ne(SysMenu::getApiMethod, "")
                        .orderByDesc(SysMenu::getApiPath)
                        .orderByAsc(SysMenu::getSort)
                        .orderByAsc(SysMenu::getId)).stream()
                .filter(menu -> StringUtils.hasText(menu.getApiPath()))
                .filter(menu -> StringUtils.hasText(menu.getApiMethod()))
                .toList();
    }

    private List<Long> listAuthorizedMenuIds(Long userId) {
        List<SysRole> roles = roleService.listActiveRolesByUserId(userId);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roles.stream().map(SysRole::getId).toList();
        List<SysRoleMenu> roleMenus = sysRoleMenuDao.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds));
        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
    }
}
