package cn.guet.feishu.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskAssignment {
    private Long id;
    private String assignmentId;
    private String taskId;
    private String userId;
    private String assignedBy;
    private Integer status;
    private Integer progress;
    private BigDecimal actualHours;
    private LocalDateTime assignTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

