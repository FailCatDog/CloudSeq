package cn.guet.feishu.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateProjectRequestDTO {
    
    private String projectName;
    
    private String description;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer status;
    
    private Integer maxGroupSize;
    
    private Integer minGroupSize;
    
    private LocalDateTime groupDeadline;
    
    private Integer allowStudentCreateGroup;
}

