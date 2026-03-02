package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class ProjectListItemDTO {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目编码
     */
    private String projectCode;
}

