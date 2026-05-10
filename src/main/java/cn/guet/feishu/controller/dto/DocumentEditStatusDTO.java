package cn.guet.feishu.controller.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentEditStatusDTO {
    /**
     * 文档ID
     */
    private String documentId;
    /**
     * 是否被锁定
     */
    private Boolean locked;
    /**
     * 正在编辑的用户ID
     */
    private String editingUserId;
    /**
     * 正在编辑的用户名
     */
    private String editingUsername;
    /**
     * 当前锁开始时间
     */
    private LocalDateTime editingStartTime;
    /**
     * 锁是否已过期
     */
    private Boolean expired;
}
