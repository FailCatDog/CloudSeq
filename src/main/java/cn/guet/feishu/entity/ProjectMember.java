package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMember {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String projectMemberId;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 在项目中的角色：OWNER-创建者，MEMBER-成员
     */
    private String roleInProject;
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    /**
     * 状态：1-活跃，0-已退出
     */
    private Integer status;
    /**
     * 邀请人ID
     */
    private String invitedBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

