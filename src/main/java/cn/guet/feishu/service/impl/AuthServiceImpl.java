package cn.guet.feishu.service.impl;

import cn.guet.feishu.controller.dto.LoginRequestDTO;
import cn.guet.feishu.controller.dto.LoginResponseDTO;
import cn.guet.feishu.controller.dto.RegisterRequestDTO;
import cn.guet.feishu.service.AuthService;
import cn.guet.feishu.common.constant.RoleConstant;
import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.common.result.ResultCode;
import cn.guet.feishu.common.util.JwtUtil;
import cn.guet.feishu.common.util.PasswordUtil;
import cn.guet.feishu.entity.User;
import cn.guet.feishu.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public void register(RegisterRequestDTO request) {
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
        }

        if (userMapper.selectByStudentId(request.getStudentId()) != null) {
            throw new BusinessException("学号已存在");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(RoleConstant.STUDENT);
        user.setStudentId(request.getStudentId());
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userMapper.selectByUsername(request.getUsername());
        
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode(), "用户名或密码错误");
        }

        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode(), "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        return new LoginResponseDTO(token, user.getUserId(), user.getUsername(), user.getRealName(), user.getRole());
    }
}


