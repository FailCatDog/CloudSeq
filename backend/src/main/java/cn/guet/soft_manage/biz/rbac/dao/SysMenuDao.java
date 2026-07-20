package cn.guet.soft_manage.biz.rbac.dao;

import cn.guet.soft_manage.biz.rbac.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单与权限 Mapper
 */
@Mapper
public interface SysMenuDao extends BaseMapper<SysMenu> {
}
