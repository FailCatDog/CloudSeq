package cn.guet.feishu.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupDocument {
    /**
     * 物理主键
     */
    private Long id;
    /**
     * 逻辑主键（UUID）
     */
    private String documentId;
    /**
     * 所属小组ID
     */
    private String groupId;
    /**
     * 所属文件夹ID（为空表示根目录）
     */
    private String folderId;
    /**
     * 文档名称
     */
    private String documentName;
    /**
     * 文件类型（pdf/docx/xlsx等）
     */
    private String fileType;
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    /**
     * 文件存储路径
     */
    private String filePath;
    /**
     * 文档描述
     */
    private String description;
    /**
     * 创建者ID（组长）
     */
    private String creatorId;
    /**
     * 下载次数
     */
    private Integer downloadCount;
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

