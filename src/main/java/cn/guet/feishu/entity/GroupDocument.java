package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupDocument {
    private Long id;
    private String documentId;
    private String groupId;
    private String folderId;
    private String documentName;
    private String fileType;
    private Long fileSize;
    private String filePath;
    private String description;
    private String creatorId;
    private Integer downloadCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

