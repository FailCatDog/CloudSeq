package cn.guet.soft_manage.biz.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 登录返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private Long id;

    private String username;

    private Integer role;

    private String nickName;

    private String realName;

    private String avatarUrl;

    private String token;
}
