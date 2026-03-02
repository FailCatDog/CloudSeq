package cn.guet.feishu.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TaskAssignment {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String assignmentId;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 被分配人ID
     */
    private String userId;
    /**
     * 分配人ID
     */
    private String assignedBy;
    /**
     * 个人完成状态：0-待开始，1-进行中，2-已完成
     */
    private Integer status;
    /**
     * 个人进度百分比（0-100）
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
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

