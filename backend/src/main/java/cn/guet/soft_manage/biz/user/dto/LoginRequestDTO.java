package cn.guet.soft_manage.biz.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 登录请求
 */
@Data
public class LoginRequestDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
