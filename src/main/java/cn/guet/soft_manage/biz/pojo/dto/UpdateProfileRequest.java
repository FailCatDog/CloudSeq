package cn.guet.soft_manage.biz.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 修改个人信息请求
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    private String nickName;

    private String realName;

    private String bio;

    private String avatarUrl;
}
