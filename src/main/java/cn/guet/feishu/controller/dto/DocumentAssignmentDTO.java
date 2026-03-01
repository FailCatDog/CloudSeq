package cn.guet.feishu.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentAssignmentDTO {
    private String assignmentId;
    private String documentId;
    private String userId;
    private String username;
    private String realName;
    private String assignedBy;
    private String permission;
    private LocalDateTime assignTime;
}

