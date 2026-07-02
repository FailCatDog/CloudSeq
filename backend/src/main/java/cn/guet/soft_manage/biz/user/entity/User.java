package cn.guet.soft_manage.biz.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 用户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sm_user")
public class User {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String password;

    private String role;

    private String studentNo;

    private String nickName;

    private String realName;

    private String bio;

    private String avatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    private Integer isActive;

    @TableLogic
    private Integer delFlag;

    @TableField(value = "create_user", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Long createUser;

    @TableField(value = "create_date", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @TableField(value = "update_user", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(value = "update_date", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Version
    private Long version;
}
