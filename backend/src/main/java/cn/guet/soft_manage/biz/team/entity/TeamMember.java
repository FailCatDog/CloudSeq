package cn.guet.soft_manage.biz.team.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 小组成员实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sm_team_member")
public class TeamMember {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long teamId;

    private Long userId;

    private Integer isLeader;

    private String memberStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leftDate;

    @TableField(exist = false)
    private Integer oneActive;

    @TableLogic
    private Integer delFlag;

    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Version
    private Long version;
}
