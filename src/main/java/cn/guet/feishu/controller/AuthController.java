package cn.guet.feishu.controller;

import cn.guet.feishu.controller.dto.LoginRequestDTO;
import cn.guet.feishu.controller.dto.LoginResponseDTO;
import cn.guet.feishu.controller.dto.RegisterRequestDTO;
import cn.guet.feishu.service.AuthService;
import cn.guet.feishu.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     * 
     * @param request 注册请求，包含用户名、密码、真实姓名、学号
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录响应，包含 token、用户ID、用户名、真实姓名、角色
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);
        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }
}


