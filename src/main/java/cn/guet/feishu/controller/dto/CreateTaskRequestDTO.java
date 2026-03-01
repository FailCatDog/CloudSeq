package cn.guet.feishu.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateTaskRequestDTO {
    
    @NotBlank(message = "任务标题不能为空")
    private String taskTitle;
    
    private String taskDescription;
    
    @NotBlank(message = "项目ID不能为空")
    private String projectId;
    
    private String groupId;
    
    @NotNull(message = "优先级不能为空")
    private Integer priority;
    
    private String parentTaskId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private BigDecimal estimatedHours;
    
    private List<String> tags;
    
    private List<String> assignToUserIds;
}

