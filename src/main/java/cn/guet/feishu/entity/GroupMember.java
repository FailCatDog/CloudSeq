package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupMember {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String groupMemberId;
    /**
     * 小组ID
     */
    private String groupId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 角色：LEADER-组长，MEMBER-组员
     */
    private String roleInGroup;
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    /**
     * 状态：1-活跃，0-已退出
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

