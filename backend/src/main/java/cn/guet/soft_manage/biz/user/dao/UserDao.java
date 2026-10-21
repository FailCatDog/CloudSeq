package cn.guet.soft_manage.biz.user.dao;

import cn.guet.soft_manage.biz.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户 Mapper
 */
@Mapper
public interface UserDao extends BaseMapper<User> {
}
