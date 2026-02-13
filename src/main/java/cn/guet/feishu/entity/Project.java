package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Project {
    private Long id;
    private String projectId;
    private String projectName;
    private String projectCode;
    private String description;
    private String creatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer maxGroupSize;
    private Integer minGroupSize;
    private LocalDateTime groupDeadline;
    private Integer allowStudentCreateGroup;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

