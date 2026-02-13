package cn.guet.feishu.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateProjectRequestDTO {
    
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer maxGroupSize;
    
    private Integer minGroupSize;
    
    private LocalDateTime groupDeadline;
    
    private Integer allowStudentCreateGroup;
}

