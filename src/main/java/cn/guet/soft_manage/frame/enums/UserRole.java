package cn.guet.soft_manage.frame.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    STUDENT("STUDENT", 2),
    TEACHER("TEACHER", 1),
    ;

    private final String role;
    private final Integer code;

    public static String getRoleName(Integer code) {
        for (UserRole role : values()) {
            if (role.code.equals(code)) return role.getRole();
        }

        return null;
    }

    public static Integer getRoleCode(String role) {
        for (UserRole userRole : values()) {
            if (userRole.role.equals(role)) return userRole.getCode();
        }

        return null;
    }
}
