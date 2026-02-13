package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String userId;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String studentId;
    private String teacherId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


