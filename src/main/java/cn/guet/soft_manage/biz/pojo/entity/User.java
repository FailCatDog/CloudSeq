package cn.guet.soft_manage.biz.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
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

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private Integer role;

    private String studentNo;

    private String nickName;

    private String realName;

    private String bio;

    private String avatarUrl;

    private LocalDateTime lastLoginAt;

    private Integer isActive;

    @TableLogic
    private Integer delFlag;

    @TableField(value = "create_user", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private Long createUser;

    @TableField(value = "create_date", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createDate;

    @TableField(value = "update_user", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(value = "update_date", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateDate;

    @Version
    private Long version;
}
