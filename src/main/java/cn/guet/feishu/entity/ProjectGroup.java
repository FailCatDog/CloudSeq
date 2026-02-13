package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectGroup {
    private Long id;
    private String groupId;
    private String projectId;
    private String groupName;
    private String groupCode;
    private String description;
    private String leaderId;
    private String creatorId;
    private Integer currentMembers;
    private Integer maxMembers;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

