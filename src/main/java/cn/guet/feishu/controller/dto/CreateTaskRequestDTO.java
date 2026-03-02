package cn.guet.feishu.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateTaskRequestDTO {
    /**
     * 任务标题
     */
    @NotBlank(message = "任务标题不能为空")
    private String taskTitle;
    /**
     * 任务描述
     */
    private String taskDescription;
    /**
     * 项目ID
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;
    /**
     * 小组ID
     */
    private String groupId;
    /**
     * 优先级
     */
    @NotNull(message = "优先级不能为空")
    private Integer priority;
    /**
     * 父任务ID
     */
    private String parentTaskId;
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
    /**
     * 分配给的用户ID列表
     */
    private List<String> assignToUserIds;
}

