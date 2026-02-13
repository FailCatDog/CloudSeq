package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.LoginRequestDTO;
import cn.guet.feishu.controller.dto.LoginResponseDTO;
import cn.guet.feishu.controller.dto.RegisterRequestDTO;

public interface AuthService {
    
    void register(RegisterRequestDTO request);
    
    LoginResponseDTO login(LoginRequestDTO request);
}


