package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateTaskRequestDTO {
    /**
     * 任务标题
     */
    private String taskTitle;
    /**
     * 任务描述
     */
    private String taskDescription;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 截止时间
     */
    private LocalDateTime endTime;
    /**
     * 预计工时
     */
    private BigDecimal estimatedHours;
    /**
     * 任务标签
     */
    private List<String> tags;
}

