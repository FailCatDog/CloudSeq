package cn.guet.soft_manage.biz.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作区文档资产（图片、附件等，二进制存 MinIO）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("fs_workspace_asset")
public class WorkspaceAsset {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long workspaceId;

    private Long nodeId;

    /** MinIO object key，不含桶名 */
    private String storageKey;

    private String originalName;

    private String contentType;

    private Long sizeBytes;

    /** IMAGE / ATTACHMENT */
    private String assetType;

    @TableLogic
    private Integer delFlag;

    private Long createUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    private Long updateUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
}
