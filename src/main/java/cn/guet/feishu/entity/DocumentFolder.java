package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentFolder {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String folderId;
    /**
     * 所属小组ID
     */
    private String groupId;
    /**
     * 文件夹名称
     */
    private String folderName;
    /**
     * 父文件夹ID（为空表示根目录）
     */
    private String parentFolderId;
    /**
     * 创建者ID（组长）
     */
    private String creatorId;
    /**
     * 状态：1-正常，2-已删除
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

