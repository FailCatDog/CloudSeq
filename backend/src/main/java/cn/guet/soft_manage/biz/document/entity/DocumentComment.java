package cn.guet.soft_manage.biz.document.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档评论（块级锚点；预留选区/回复/状态等扩展字段）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("sm_document_comment")
public class DocumentComment {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long workspaceId;

    private Long nodeId;

    /** BLOCK / RANGE（当前仅用 BLOCK） */
    private String anchorType;

    /** ProseMirror 锚点起始位置 */
    private Integer anchorPos;

    /** 选区结束位置，RANGE 时用，当前未启用 */
    private Integer anchorTo;

    /** 锚点原文快照 */
    private String quoteText;

    /** 评论正文 */
    private String content;

    /** 预留：讨论串 ID */
    private String threadId;

    /** 预留：父评论 ID */
    private Long parentCommentId;

    /** 预留：OPEN / RESOLVED */
    private String commentStatus;

    /** 预留：扩展 JSON */
    private String extraJson;

    @TableLogic
    private Integer delFlag;

    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;

    @Version
    private Long version;
}
