package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentEdit {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String editId;
    /**
     * 文档ID
     */
    private String documentId;
    /**
     * 文档内容
     */
    private String content;
    /**
     * 正在编辑的用户ID
     */
    private String editingUserId;
    /**
     * 开始编辑时间
     */
    private LocalDateTime editingStartTime;
    /**
     * 状态：1-正常，0-删除
     */
    private Integer status;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

