package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dao.SysRoleDao;
import cn.guet.soft_manage.biz.rbac.dao.SysRoleMenuDao;
import cn.guet.soft_manage.biz.rbac.dto.request.RoleSaveRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.RoleSummaryDTO;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.entity.SysRoleMenu;
import cn.guet.soft_manage.biz.rbac.service.IAdminRoleService;
import cn.guet.soft_manage.biz.rbac.support.AdminAuthSupport;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminRoleServiceImpl implements IAdminRoleService {

    @Resource
    private SysRoleDao sysRoleDao;

    @Resource
    private SysRoleMenuDao sysRoleMenuDao;

    @Resource
    private AdminAuthSupport adminAuthSupport;

    @Override
    public List<RoleSummaryDTO> list(String status) {
        adminAuthSupport.requireAdmin();
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getRoleSort)
                .orderByAsc(SysRole::getId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysRole::getStatus, status.trim());
        }
        return sysRoleDao.selectList(wrapper).stream().map(this::toSummary).toList();
    }

    @Override
    public RoleSummaryDTO getById(Long id) {
        adminAuthSupport.requireAdmin();
        return toSummary(requireRole(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummaryDTO create(RoleSaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        assertRoleKeyUnique(request.getRoleKey(), null);
        SysRole role = new SysRole();
        applyRequest(role, request);
        if (!StringUtils.hasText(role.getStatus())) {
            role.setStatus(CacheCode.SYS_STATUS_ACTIVE.getCode());
        }
        if (role.getRoleSort() == null) {
            role.setRoleSort(0);
        }
        sysRoleDao.insert(role);
        return toSummary(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleSummaryDTO update(Long id, RoleSaveRequestDTO request) {
        adminAuthSupport.requireAdmin();
        SysRole role = requireRole(id);
        if (request.getVersion() != null && !Objects.equals(role.getVersion(), request.getVersion())) {
            throw new BusinessException(BizResponseCode.ROLE_STALE);
        }
        assertRoleKeyUnique(request.getRoleKey(), id);
        applyRequest(role, request);
        sysRoleDao.updateById(role);
        return toSummary(requireRole(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        adminAuthSupport.requireAdmin();
        SysRole role = requireRole(id);
        role.setStatus(status);
        sysRoleDao.updateById(role);
    }

    @Override
    public List<Long> listMenuIds(Long roleId) {
        adminAuthSupport.requireAdmin();
        requireRole(roleId);
        return sysRoleMenuDao.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceMenuIds(Long roleId, List<Long> menuIds) {
        adminAuthSupport.requireAdmin();
        requireRole(roleId);
        sysRoleMenuDao.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(menuIds);
        for (Long menuId : uniqueIds) {
            if (menuId == null) {
                continue;
            }
            sysRoleMenuDao.insert(SysRoleMenu.builder()
                    .roleId(roleId)
                    .menuId(menuId)
                    .build());
        }
    }

    private SysRole requireRole(Long id) {
        SysRole role = sysRoleDao.selectById(id);
        if (role == null) {
            throw new BusinessException(BizResponseCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void assertRoleKeyUnique(String roleKey, Long excludeId) {
        if (!StringUtils.hasText(roleKey)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey.trim());
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        if (sysRoleDao.exists(wrapper)) {
            throw new BusinessException(BizResponseCode.ROLE_KEY_EXISTS);
        }
    }

    private void applyRequest(SysRole role, RoleSaveRequestDTO request) {
        role.setRoleKey(request.getRoleKey().trim());
        role.setRoleName(request.getRoleName().trim());
        role.setRoleSort(request.getRoleSort());
        role.setDataScope(request.getDataScope().trim());
        role.setHomePath(trimToNull(request.getHomePath()));
        role.setStatus(trimToNull(request.getStatus()));
        role.setRemark(trimToNull(request.getRemark()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private RoleSummaryDTO toSummary(SysRole role) {
        return RoleSummaryDTO.builder()
                .id(role.getId())
                .roleKey(role.getRoleKey())
                .roleName(role.getRoleName())
                .roleSort(role.getRoleSort())
                .dataScope(role.getDataScope())
                .homePath(role.getHomePath())
                .status(role.getStatus())
                .remark(role.getRemark())
                .version(role.getVersion())
                .build();
    }
}
