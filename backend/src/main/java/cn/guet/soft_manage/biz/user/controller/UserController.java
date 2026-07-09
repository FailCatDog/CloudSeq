package cn.guet.soft_manage.biz.user.controller;

import cn.guet.soft_manage.biz.user.dto.LoginRequestDTO;
import cn.guet.soft_manage.biz.user.dto.LoginResponseDTO;
import cn.guet.soft_manage.biz.user.dto.ProfileStatusAggregate;
import cn.guet.soft_manage.biz.user.dto.RegisterRequestDTO;
import cn.guet.soft_manage.biz.user.entity.User;
import cn.guet.soft_manage.biz.user.param.UserParam;
import cn.guet.soft_manage.biz.user.service.UserService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
    public Response<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return Response.success(userService.login(request));
    }

    @PostMapping("/register")
    public Response<Void> register(@Valid @RequestBody RegisterRequestDTO request) {
        userService.register(request);
        return Response.success();
    }

    @GetMapping("/profile/{userId}")
    public Response<User> profile(@PathVariable Long userId) {
        return Response.success(userService.getProfile(userId));
    }

    @PatchMapping("/profile")
    public Response<Void> updateProfile(@Valid @RequestBody UserParam request) {
        userService.updateProfile(request);
        return Response.success();
    }

    @GetMapping("/profile/status")
    public Response<ProfileStatusAggregate> profileStatus() {
        return Response.success(userService.getProfileStatus());
    }
}
