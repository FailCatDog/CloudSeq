package cn.guet.soft_manage.biz.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 注册请求
 */
@Data
public class RegisterRequestDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotNull(message = "角色不能为空")
    private String role;

    @NotNull(message = "学号不能为空")
    private String studentNo;

    private String nickName;

    private String realName;

    private String bio;

    private String avatarUrl;
}
