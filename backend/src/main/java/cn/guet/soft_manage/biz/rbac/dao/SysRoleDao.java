package cn.guet.soft_manage.biz.rbac.dao;

import cn.guet.soft_manage.biz.rbac.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统角色 Mapper
 */
@Mapper
public interface SysRoleDao extends BaseMapper<SysRole> {
}
