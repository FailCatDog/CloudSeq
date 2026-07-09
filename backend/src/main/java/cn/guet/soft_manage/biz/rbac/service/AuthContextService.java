package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.AuthContextDTO;

/**
 * 组装登录授权上下文
 */
public interface AuthContextService {

    AuthContextDTO build(Long userId);

    AuthContextDTO build(Long userId, String preferredRoleKey);
}
