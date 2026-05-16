package cn.guet.soft_manage.biz.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 个人信息返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {

    private Long id;

    private String username;

    private Integer role;

    private String studentNo;

    private String nickName;

    private String realName;

    private String bio;

    private String avatarUrl;

    private LocalDateTime lastLoginAt;

    private Integer isActive;
}
