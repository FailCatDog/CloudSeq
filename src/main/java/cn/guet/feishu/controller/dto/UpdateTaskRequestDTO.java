package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateTaskRequestDTO {
    private String taskTitle;
    private String taskDescription;
    private Integer priority;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal estimatedHours;
    private List<String> tags;
}

