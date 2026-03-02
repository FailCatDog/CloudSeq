package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentAssignment {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String assignmentId;
    /**
     * 文档ID
     */
    private String documentId;
    /**
     * 被分配的用户ID
     */
    private String userId;
    /**
     * 分配者ID（组长）
     */
    private String assignedBy;
    /**
     * 权限：VIEW-查看，EDIT-编辑
     */
    private String permission;
    /**
     * 分配时间
     */
    private LocalDateTime assignTime;
}

