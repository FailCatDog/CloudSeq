package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailDTO {
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
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private List<TaskAssignmentDTO> assignments;
    private List<TaskDetailDTO> subTasks;
    private List<String> dependOnTaskIds;
}

