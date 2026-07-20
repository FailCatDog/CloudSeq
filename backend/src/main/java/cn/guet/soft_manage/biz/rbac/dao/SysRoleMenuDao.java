package cn.guet.soft_manage.biz.rbac.dao;

import cn.guet.soft_manage.biz.rbac.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单/权限关联 Mapper
 */
@Mapper
public interface SysRoleMenuDao extends BaseMapper<SysRoleMenu> {
}
