package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.UpdateUserInfoRequestDTO;
import cn.guet.feishu.entity.User;

public interface UserService {
    
    User getUserInfo(String userId);
    
    void updateUserInfo(String userId, UpdateUserInfoRequestDTO request);
}


