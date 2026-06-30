package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建文档评论（块级锚点）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCommentCreateRequestDTO {

    /** ProseMirror 块起始位置，必填 */
    private Integer anchorPos;

    /** 锚点原文快照，可选 */
    private String quoteText;

    /** 评论正文，必填 */
    private String content;
}
