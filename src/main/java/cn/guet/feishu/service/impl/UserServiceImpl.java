package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.common.result.ResultCode;
import cn.guet.feishu.controller.dto.UpdateUserInfoRequestDTO;
import cn.guet.feishu.entity.User;
import cn.guet.feishu.mapper.UserMapper;
import cn.guet.feishu.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getUserInfo(String userId) {
        User user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUserInfo(String userId, UpdateUserInfoRequestDTO request) {
        User existUser = userMapper.selectByUserId(userId);
        if (existUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        User user = new User();
        user.setUserId(userId);
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAvatar(request.getAvatar());
        
        userMapper.updateByUserId(user);
    }
}


