package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dto.MenuTreeDTO;
import cn.guet.soft_manage.biz.rbac.entity.SysMenu;
import cn.guet.soft_manage.biz.rbac.service.IMenuService;
import cn.guet.soft_manage.biz.rbac.service.IPermissionService;
import cn.guet.soft_manage.frame.enums.CacheCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements IMenuService {

    private static final long ROOT_PARENT_ID = 0L;

    @Resource
    private IPermissionService permissionService;

    @Override
    public List<MenuTreeDTO> buildMenuTree(Long userId) {
        List<SysMenu> menus = permissionService.listAuthorizedMenus(userId).stream()
                .filter(this::isNavMenu)
                .toList();
        if (menus.isEmpty()) {
            return List.of();
        }

        Map<Long, List<SysMenu>> childrenByParent = menus.stream()
                .collect(Collectors.groupingBy(menu -> normalizeParentId(menu.getParentId())));

        return buildChildren(ROOT_PARENT_ID, childrenByParent);
    }

    private boolean isNavMenu(SysMenu menu) {
        String menuType = menu.getMenuType();
        if (!CacheCode.MENU_TYPE_DIR.getCode().equals(menuType)
                && !CacheCode.MENU_TYPE_MENU.getCode().equals(menuType)) {
            return false;
        }
        return CacheCode.MENU_VISIBLE_SHOW.getCode().equals(menu.getVisible());
    }

    private long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private List<MenuTreeDTO> buildChildren(long parentId, Map<Long, List<SysMenu>> childrenByParent) {
        return childrenByParent.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId))
                .map(menu -> toTreeNode(menu, childrenByParent))
                .toList();
    }

    private MenuTreeDTO toTreeNode(SysMenu menu, Map<Long, List<SysMenu>> childrenByParent) {
        List<MenuTreeDTO> children = buildChildren(menu.getId(), childrenByParent);
        return MenuTreeDTO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .routeMatch(menu.getRouteMatch())
                .perms(menu.getPerms())
                .icon(menu.getIcon())
                .sort(menu.getSort())
                .children(children.isEmpty() ? new ArrayList<>() : new ArrayList<>(children))
                .build();
    }
}
