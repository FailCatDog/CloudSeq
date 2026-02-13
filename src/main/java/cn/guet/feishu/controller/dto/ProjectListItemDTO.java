package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class ProjectListItemDTO {
    private Long id;
    private String projectId;
    private String projectName;
    private String projectCode;
}

