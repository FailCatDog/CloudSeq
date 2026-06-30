package cn.guet.soft_manage.biz.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档评论详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCommentDTO {

    private Long id;

    private Long nodeId;

    private Long workspaceId;

    private String anchorType;

    private Integer anchorPos;

    /** 预留：选区结束位置，当前块级评论为 null */
    private Integer anchorTo;

    private String quoteText;

    private String content;

    /** 预留：讨论串 ID */
    private String threadId;

    /** 预留：父评论 ID */
    private Long parentCommentId;

    /** 预留：评论状态 */
    private String commentStatus;

    private Long authorId;

    private String authorName;

    private String authorAvatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;
}
