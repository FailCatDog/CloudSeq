package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String userId;
    /**
     * 用户名（登录账号）
     */
    private String username;
    /**
     * 密码（BCrypt加密）
     */
    private String password;
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
    /**
     * 角色：TEACHER-教师，STUDENT-学生
     */
    private String role;
    /**
     * 学号（学生专用）
     */
    private String studentId;
    /**
     * 教师编号（教师专用）
     */
    private String teacherId;
    /**
     * 账号状态：1-正常，0-禁用
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

