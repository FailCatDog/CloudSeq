package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.UpdateProfileRequest;
import cn.guet.soft_manage.biz.service.UserService;
import cn.guet.soft_manage.biz.pojo.vo.LoginVO;
import cn.guet.soft_manage.biz.pojo.vo.UserProfileVO;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户控制器
 */
@RestController
@RequestMapping("/api/account")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Response<LoginVO> login(@Valid @RequestBody LoginRequestDTO request) {
        return Response.success(userService.login(request));
    }

    @PostMapping("/register")
    public Response<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        userService.register(request);
        return Response.success();
    }

    @GetMapping("/profile/{userId}")
    public Response<UserProfileVO> profile(@PathVariable Long userId) {
        return Response.success(userService.getProfile(userId));
    }

    @PutMapping("/profile/{userId}")
    public Response<Void> updateProfile(@PathVariable Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(userId, request);
        return Response.success();
    }
}
