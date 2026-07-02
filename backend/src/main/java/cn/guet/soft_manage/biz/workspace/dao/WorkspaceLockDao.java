package cn.guet.soft_manage.biz.workspace.dao;

import cn.guet.soft_manage.biz.workspace.entity.WorkspaceLock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 工作区文档编辑锁 DAO
 */
@Mapper
public interface WorkspaceLockDao extends BaseMapper<WorkspaceLock> {
}
