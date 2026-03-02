package cn.guet.feishu.controller;

import cn.guet.feishu.common.result.Result;
import cn.guet.feishu.controller.dto.UpdateUserInfoRequestDTO;
import cn.guet.feishu.entity.User;
import cn.guet.feishu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     * 
     * @param request HTTP 请求，从中获取当前用户ID
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        User user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     * 
     * @param request HTTP 请求，从中获取当前用户ID
     * @param updateRequest 更新请求，包含真实姓名、邮箱、手机号、头像
     * @return 操作结果
     */
    @PutMapping("/info")
    public Result<Void> updateUserInfo(HttpServletRequest request, @RequestBody UpdateUserInfoRequestDTO updateRequest) {
        String userId = (String) request.getAttribute("userId");
        userService.updateUserInfo(userId, updateRequest);
        return Result.success();
    }
}


