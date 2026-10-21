package cn.guet.soft_manage.biz.rbac.support;

import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import cn.guet.soft_manage.biz.rbac.service.IRoleService;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 管理端接口鉴权：要求当前用户拥有 ADMIN 角色
 */
@Component
public class AdminAuthSupport {

    @Resource
    private IRoleService roleService;

    public void requireAdmin() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }
        List<SysRole> roles = roleService.listActiveRolesByUserId(userId);
        boolean admin = roles.stream()
                .anyMatch(role -> Objects.equals(role.getRoleKey(), CacheCode.USER_ROLE_ADMIN.getCode()));
        if (!admin) {
            throw new BusinessException(BizResponseCode.ADMIN_REQUIRED);
        }
    }
}
