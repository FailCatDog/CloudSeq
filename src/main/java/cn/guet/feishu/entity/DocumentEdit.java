package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentEdit {
    private Long id;
    private String editId;
    private String documentId;
    private String content;
    private String editingUserId;
    private LocalDateTime editingStartTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

