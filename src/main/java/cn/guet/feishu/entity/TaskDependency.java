package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDependency {
    private Long id;
    private String dependencyId;
    private String taskId;
    private String dependOnTaskId;
    private Integer dependencyType;
    private LocalDateTime createTime;
}

