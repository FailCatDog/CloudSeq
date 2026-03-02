package cn.guet.feishu.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProjectTask {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String taskId;
    /**
     * 所属项目ID
     */
    private String projectId;
    /**
     * 所属小组ID（为空表示项目级任务）
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
     * 任务编码（自动生成）
     */
    private String taskCode;
    /**
     * 优先级：0-高，1-中，2-低
     */
    private Integer priority;
    /**
     * 状态：0-待开始，1-进行中，2-已完成，3-已暂停，4-已取消
     */
    private Integer status;
    /**
     * 进度百分比（0-100）
     */
    private Integer progress;
    /**
     * 父任务ID（用于子任务）
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
     * 预计工时（小时）
     */
    private BigDecimal estimatedHours;
    /**
     * 实际工时（小时）
     */
    private BigDecimal actualHours;
    /**
     * 任务标签（JSON数组）
     */
    private String tags;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

