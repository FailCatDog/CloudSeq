package cn.guet.soft_manage.biz.team.dao;

import cn.guet.soft_manage.biz.team.entity.TeamMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 小组成员 Mapper
 */
@Mapper
public interface TeamMemberDao extends BaseMapper<TeamMember> {
}
