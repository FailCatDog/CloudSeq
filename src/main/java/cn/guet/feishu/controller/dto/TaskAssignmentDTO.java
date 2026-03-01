package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskAssignmentDTO {
    private String assignmentId;
    private String taskId;
    private String userId;
    private String username;
    private String realName;
    private String assignedBy;
    private Integer status;
    private Integer progress;
    private BigDecimal actualHours;
    private LocalDateTime assignTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}

