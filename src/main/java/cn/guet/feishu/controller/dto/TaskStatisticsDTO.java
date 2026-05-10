package cn.guet.feishu.controller.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class TaskStatisticsDTO {
    /**
     * 总任务数
     */
    private Long totalTasks;
    /**
     * 各状态任务数
     */
    private Map<Integer, Long> statusCounts = new HashMap<>();
    /**
     * 高/中/低优先级任务数
     */
    private Map<Integer, Long> priorityCounts = new HashMap<>();
    /**
     * 已完成任务数
     */
    private Long completedTasks;
    /**
     * 进行中任务数
     */
    private Long inProgressTasks;
    /**
     * 待开始任务数
     */
    private Long pendingTasks;
    /**
     * 任务完成率
     */
    private BigDecimal completionRate;
    /**
     * 平均进度
     */
    private BigDecimal averageProgress;
    /**
     * 总预计工时
     */
    private BigDecimal totalEstimatedHours;
    /**
     * 总实际工时
     */
    private BigDecimal totalActualHours;
}
