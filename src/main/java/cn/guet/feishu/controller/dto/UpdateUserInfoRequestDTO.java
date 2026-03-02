package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequestDTO {
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 头像URL
     */
    private String avatar;
}

