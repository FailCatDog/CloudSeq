package cn.guet.soft_manage.biz.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区目录树节点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceNodeTreeDTO {

    private Long id;

    private Long parentId;

    private String nodeType;

    private String title;

    private Integer sortOrder;

    /** 文档节点摘要（不含正文） */
    private String summary;

    private Integer charCount;

    private Integer contentBytes;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime contentUpdateDate;

    private List<WorkspaceNodeTreeDTO> children;
}
