package cn.guet.feishu.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateProjectRequestDTO {
    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    private String projectName;
    /**
     * 项目描述
     */
    private String description;
    /**
     * 项目开始时间
     */
    private LocalDateTime startTime;
    /**
     * 项目结束时间
     */
    private LocalDateTime endTime;
    /**
     * 最大小组人数
     */
    private Integer maxGroupSize;
    /**
     * 最小小组人数
     */
    private Integer minGroupSize;
    /**
     * 小组创建截止时间
     */
    private LocalDateTime groupDeadline;
    /**
     * 是否允许学生创建小组
     */
    private Integer allowStudentCreateGroup;
}

