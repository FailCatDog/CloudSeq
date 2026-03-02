package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class AssignDocumentRequestDTO {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 权限：VIEW-查看，EDIT-编辑
     */
    private String permission;
}

