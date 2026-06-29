package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.LoginResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.pojo.param.UserParam;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户服务
 */
public interface UserService {

    LoginResponseDTO login(LoginRequestDTO request);

    void register(RegisterRequestDTO request);

    User getProfile(Long userId);

    void updateProfile(UserParam request);
}
