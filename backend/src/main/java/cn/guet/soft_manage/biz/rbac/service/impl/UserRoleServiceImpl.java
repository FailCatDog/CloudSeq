package cn.guet.soft_manage.biz.rbac.service.impl;

import cn.guet.soft_manage.biz.rbac.dao.SysUserRoleDao;
import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.entity.SysUserRole;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import cn.guet.soft_manage.biz.rbac.service.IUserRoleService;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UserRoleServiceImpl implements IUserRoleService {

    @Resource
    private SysUserRoleDao sysUserRoleDao;

    @Resource
    private IRoleService roleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRole(Long userId, String roleKey) {
        if (userId == null || !StringUtils.hasText(roleKey)) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
        SysRole role = roleService.getByRoleKey(roleKey.trim());
        if (role == null) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }
        boolean exists = sysUserRoleDao.exists(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, role.getId()));
        if (exists) {
            return;
        }
        SysUserRole userRole = SysUserRole.builder()
                .userId(userId)
                .roleId(role.getId())
                .build();
        sysUserRoleDao.insert(userRole);
    }
}
