package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentAssignment {
    private Long id;
    private String assignmentId;
    private String documentId;
    private String userId;
    private String assignedBy;
    private String permission;
    private LocalDateTime assignTime;
}

