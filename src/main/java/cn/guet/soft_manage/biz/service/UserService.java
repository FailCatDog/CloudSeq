package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.UpdateProfileRequest;
import cn.guet.soft_manage.biz.pojo.vo.LoginVO;
import cn.guet.soft_manage.biz.pojo.vo.UserProfileVO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户服务
 */
public interface UserService {

    LoginVO login(LoginRequestDTO request);

    void register(RegisterRequestDTO request);

    UserProfileVO getProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);
}
