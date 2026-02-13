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

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        authService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestParam(value = "username") String username, @RequestParam(value = "password") String password) {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername(username);
        request.setPassword(password);
        LoginResponseDTO response = authService.login(request);
        return Result.success(response);
    }
}


