package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.UpdateUserInfoRequestDTO;
import cn.guet.feishu.entity.User;

public interface UserService {
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserInfo(String userId);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param request 更新请求，包含真实姓名、邮箱、手机号、头像
     */
    void updateUserInfo(String userId, UpdateUserInfoRequestDTO request);
}


