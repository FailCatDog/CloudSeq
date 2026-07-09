package cn.guet.soft_manage.biz.rbac.support;

import cn.guet.soft_manage.biz.rbac.service.PermissionService;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 权限校验工具（Service 层手动调用）
 */
@Component
public class Authz {

    @Resource
    private PermissionService permissionService;

    public boolean hasPermission(String perm) {
        if (!StringUtils.hasText(perm)) {
            return false;
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }
        return permissionService.getPermissionCodes(userId).contains(perm.trim());
    }

    public void requirePermission(String perm) {
        if (!hasPermission(perm)) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }
    }
}
