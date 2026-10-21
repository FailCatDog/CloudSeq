package cn.guet.soft_manage.frame.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录上下文用户（JWT 载荷映射，非数据库实体）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private Long id;
    private String username;
    private String role;
    private String studentNo;
    private String nickName;
    private String realName;
    private String avatarUrl;

    public String displayName() {
        return displayNameOr("用户");
    }

    public String displayNameOr(String fallback) {
        if (realName != null && !realName.isBlank()) return realName;
        if (nickName != null && !nickName.isBlank()) return nickName;
        if (username != null && !username.isBlank()) return username;
        return fallback;
    }
}
