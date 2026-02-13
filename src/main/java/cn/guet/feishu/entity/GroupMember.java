package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupMember {
    private Long id;
    private String groupMemberId;
    private String groupId;
    private String userId;
    private String roleInGroup;
    private LocalDateTime joinTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

