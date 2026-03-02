package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskAssignmentDTO {
    /**
     * 分配ID
     */
    private String assignmentId;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 分配人ID
     */
    private String assignedBy;
    /**
     * 个人完成状态
     */
    private Integer status;
    /**
     * 个人进度百分比
     */
    private Integer progress;
    /**
     * 个人实际工时
     */
    private BigDecimal actualHours;
    /**
     * 分配时间
     */
    private LocalDateTime assignTime;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
}

