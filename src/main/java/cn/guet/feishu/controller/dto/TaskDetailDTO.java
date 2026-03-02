package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailDTO {
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 小组ID
     */
    private String groupId;
    /**
     * 任务标题
     */
    private String taskTitle;
    /**
     * 任务描述
     */
    private String taskDescription;
    /**
     * 任务编码
     */
    private String taskCode;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 进度百分比
     */
    private Integer progress;
    /**
     * 父任务ID
     */
    private String parentTaskId;
    /**
     * 创建者ID
     */
    private String creatorId;
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
     * 实际工时
     */
    private BigDecimal actualHours;
    /**
     * 任务标签
     */
    private List<String> tags;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 任务分配列表
     */
    private List<TaskAssignmentDTO> assignments;
    /**
     * 子任务列表
     */
    private List<TaskDetailDTO> subTasks;
    /**
     * 依赖的任务ID列表
     */
    private List<String> dependOnTaskIds;
}

