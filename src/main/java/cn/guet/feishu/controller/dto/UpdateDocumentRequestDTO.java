package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class UpdateDocumentRequestDTO {
    /**
     * 文档名称
     */
    private String documentName;
    /**
     * 文档描述
     */
    private String description;
    /**
     * 移动到的文件夹ID
     */
    private String folderId;
}

