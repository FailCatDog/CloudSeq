package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentFolder {
    private Long id;
    private String folderId;
    private String groupId;
    private String folderName;
    private String parentFolderId;
    private String creatorId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

