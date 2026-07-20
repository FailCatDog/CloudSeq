package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.entity.SysMenu;

import java.util.List;
import java.util.Set;

/**
 * 用户功能权限（perms）
 */
public interface IPermissionService {

    Set<String> getPermissionCodes(Long userId);

    List<SysMenu> listAuthorizedMenus(Long userId);

    /** 含 api_path 的按钮权限，供拦截器匹配 */
    List<SysMenu> listApiMenus(Long userId);

    /** 全局 API 权限规则（菜单表 api_path + api_method） */
    List<SysMenu> listGlobalApiMenuRules();
}
