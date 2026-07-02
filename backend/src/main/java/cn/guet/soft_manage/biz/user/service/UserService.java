package cn.guet.soft_manage.biz.user.service;

import cn.guet.soft_manage.biz.user.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.user.dto.LoginResponseDTO;
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
}
