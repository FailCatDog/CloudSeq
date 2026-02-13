package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMember {
    private Long id;
    private String projectMemberId;
    private String projectId;
    private String userId;
    private String roleInProject;
    private LocalDateTime joinTime;
    private Integer status;
    private String invitedBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

