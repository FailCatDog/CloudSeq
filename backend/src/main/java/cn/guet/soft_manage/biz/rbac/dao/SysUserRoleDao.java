package cn.guet.soft_manage.biz.rbac.dao;

import cn.guet.soft_manage.biz.rbac.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联 Mapper
 */
@Mapper
public interface SysUserRoleDao extends BaseMapper<SysUserRole> {
}
