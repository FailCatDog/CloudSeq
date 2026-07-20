package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dao.SysMenuDao;
import cn.guet.soft_manage.biz.rbac.dto.request.MenuSaveRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.AdminMenuNodeDTO;
import cn.guet.soft_manage.biz.rbac.entity.SysMenu;
import cn.guet.soft_manage.biz.rbac.service.IAdminMenuService;
import cn.guet.soft_manage.biz.rbac.support.AdminAuthSupport;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import cn.guet.soft_manage.frame.interceptor.PermissionInterceptor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminMenuServiceImpl implements IAdminMenuService {

    private static final long ROOT_PARENT_ID = 0L;

    @Resource
    private SysMenuDao sysMenuDao;

    @Resource
    private AdminAuthSupport adminAuthSupport;

    @Resource
    private PermissionInterceptor permissionInterceptor;

    @Override
    public List<AdminMenuNodeDTO> listTree() {
        adminAuthSupport.requireAdmin();
        List<SysMenu> menus = sysMenuDao.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort)
                .orderByAsc(SysMenu::getId));
        Map<Long, List<SysMenu>> childrenByParent = menus.stream()
                .collect(Collectors.groupingBy(menu -> normalizeParentId(menu.getParentId())));
        return buildChildren(ROOT_PARENT_ID, childrenByParent);
    }

    @Override
    public AdminMenuNodeDTO getById(Long id) {
        adminAuthSupport.requireAdmin();
        SysMenu menu = requireMenu(id);
        return toNode(menu, List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMenuNodeDTO create(MenuSaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        SysMenu menu = new SysMenu();
        applyRequest(menu, request);
        if (!StringUtils.hasText(menu.getStatus())) {
            menu.setStatus(CacheCode.SYS_STATUS_ACTIVE.getCode());
        }
        if (!StringUtils.hasText(menu.getVisible())) {
            menu.setVisible(CacheCode.MENU_VISIBLE_SHOW.getCode());
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        sysMenuDao.insert(menu);
        refreshRules();
        return toNode(menu, List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminMenuNodeDTO update(Long id, MenuSaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        SysMenu menu = requireMenu(id);
        if (request.getVersion() != null && !Objects.equals(menu.getVersion(), request.getVersion())) {
            throw new BusinessException(BizResponseCode.MENU_STALE);
        }
        applyRequest(menu, request);
        sysMenuDao.updateById(menu);
        refreshRules();
        return toNode(requireMenu(id), List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        adminAuthSupport.requireAdmin();
        requireMenu(id);
        Long childCount = sysMenuDao.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(BizResponseCode.MENU_HAS_CHILDREN);
        }
        sysMenuDao.deleteById(id);
        refreshRules();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        adminAuthSupport.requireAdmin();
        SysMenu menu = requireMenu(id);
        menu.setStatus(status);
        sysMenuDao.updateById(menu);
        refreshRules();
    }

    private SysMenu requireMenu(Long id) {
        SysMenu menu = sysMenuDao.selectById(id);
        if (menu == null) {
            throw new BusinessException(BizResponseCode.MENU_NOT_FOUND);
        }
        return menu;
    }

    private void applyRequest(SysMenu menu, MenuSaveRequestDTO request) {
        menu.setParentId(request.getParentId() == null ? ROOT_PARENT_ID : request.getParentId());
        menu.setMenuName(request.getMenuName().trim());
        menu.setMenuType(request.getMenuType().trim());
        menu.setPath(trimToNull(request.getPath()));
        menu.setRouteMatch(trimToNull(request.getRouteMatch()));
        menu.setComponent(trimToNull(request.getComponent()));
        menu.setPerms(trimToNull(request.getPerms()));
        menu.setApiMethod(trimToNull(request.getApiMethod()));
        menu.setApiPath(trimToNull(request.getApiPath()));
        menu.setIcon(trimToNull(request.getIcon()));
        menu.setSort(request.getSort());
        menu.setVisible(trimToNull(request.getVisible()));
        menu.setStatus(trimToNull(request.getStatus()));
        menu.setRemark(trimToNull(request.getRemark()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private List<AdminMenuNodeDTO> buildChildren(long parentId, Map<Long, List<SysMenu>> childrenByParent) {
        return childrenByParent.getOrDefault(parentId, List.of()).stream()
                .sorted(Comparator.comparing(SysMenu::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysMenu::getId))
                .map(menu -> toNode(menu, buildChildren(menu.getId(), childrenByParent)))
                .toList();
    }

    private AdminMenuNodeDTO toNode(SysMenu menu, List<AdminMenuNodeDTO> children) {
        return AdminMenuNodeDTO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuName(menu.getMenuName())
                .menuType(menu.getMenuType())
                .path(menu.getPath())
                .routeMatch(menu.getRouteMatch())
                .component(menu.getComponent())
                .perms(menu.getPerms())
                .apiMethod(menu.getApiMethod())
                .apiPath(menu.getApiPath())
                .icon(menu.getIcon())
                .sort(menu.getSort())
                .visible(menu.getVisible())
                .status(menu.getStatus())
                .remark(menu.getRemark())
                .version(menu.getVersion())
                .children(children == null ? new ArrayList<>() : new ArrayList<>(children))
                .build();
    }

    private void refreshRules() {
        permissionInterceptor.refreshRulesNow();
    }
}
