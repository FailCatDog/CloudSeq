package cn.guet.soft_manage.biz.user.service;

import cn.guet.soft_manage.biz.rbac.dto.AuthContextDTO;
import cn.guet.soft_manage.biz.user.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.user.dto.LoginResponseDTO;
import cn.guet.soft_manage.biz.user.dto.ProfileStatusAggregate;
import cn.guet.soft_manage.biz.user.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.biz.user.param.UserParam;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户服务
 */
public interface UserService {

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponseDTO login(LoginRequestDTO request);

    /**
     * 用户注册
     * @param request 注册请求
     */
    void register(RegisterRequestDTO request);

    /**
     * 获取用户资料
     * @param userId 用户ID
     * @return 用户信息
     */
    User getProfile(Long userId);

    /**
     * 更新当前用户资料
     * @param request 资料参数
     */
    void updateProfile(UserParam request);

    /**
     * 当前用户个人状态聚合（里程碑 + 课号/小组/审批/工作区快照）
     */
    ProfileStatusAggregate getProfileStatus();

    /**
     * 当前用户授权上下文（权限码 + 菜单树 + 首页 + 数据范围）
     */
    AuthContextDTO getAuthContext();
}
