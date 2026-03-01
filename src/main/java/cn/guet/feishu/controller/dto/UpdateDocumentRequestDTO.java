package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class UpdateDocumentRequestDTO {
    private String documentName;
    private String description;
    private String folderId;
}

