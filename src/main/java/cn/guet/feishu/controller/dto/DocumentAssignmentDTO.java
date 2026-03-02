package cn.guet.feishu.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentAssignmentDTO {
    /**
     * 分配ID
     */
    private String assignmentId;
    /**
     * 文档ID
     */
    private String documentId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 分配人ID
     */
    private String assignedBy;
    /**
     * 权限
     */
    private String permission;
    /**
     * 分配时间
     */
    private LocalDateTime assignTime;
}

