package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.MenuTreeDTO;

import java.util.List;

/**
 * 可见菜单树
 */
public interface IMenuService {

    List<MenuTreeDTO> buildMenuTree(Long userId);
}
