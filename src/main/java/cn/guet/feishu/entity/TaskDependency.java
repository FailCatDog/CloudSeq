package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDependency {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String dependencyId;
    /**
     * 当前任务ID
     */
    private String taskId;
    /**
     * 依赖的任务ID（前置任务）
     */
    private String dependOnTaskId;
    /**
     * 依赖类型：0-完成后开始（默认）
     */
    private Integer dependencyType;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

