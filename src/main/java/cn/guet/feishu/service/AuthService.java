package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.LoginRequestDTO;
import cn.guet.feishu.controller.dto.LoginResponseDTO;
import cn.guet.feishu.controller.dto.RegisterRequestDTO;

public interface AuthService {
    
    /**
     * 用户注册
     * 
     * @param request 注册请求，包含用户名、密码、真实姓名、学号
     */
    void register(RegisterRequestDTO request);
    
    /**
     * 用户登录
     * 
     * @param request 登录请求，包含用户名、密码
     * @return 登录响应，包含 token、用户信息
     */
    LoginResponseDTO login(LoginRequestDTO request);
}


