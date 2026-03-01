package cn.guet.feishu.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectTask {
    private Long id;
    private String taskId;
    private String projectId;
    private String groupId;
    private String taskTitle;
    private String taskDescription;
    private String taskCode;
    private Integer priority;
    private Integer status;
    private Integer progress;
    private String parentTaskId;
    private String creatorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal estimatedHours;
    private BigDecimal actualHours;
    private String tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

