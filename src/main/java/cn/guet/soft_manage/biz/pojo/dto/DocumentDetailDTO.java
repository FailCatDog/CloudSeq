package cn.guet.soft_manage.biz.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: Markdown 文档详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailDTO {

    private Long nodeId;

    private Long workspaceId;

    private String title;

    private String contentMd;

    private Long version;

    private Boolean canWrite;

    private Integer charCount;

    private Integer contentBytes;

    private Integer yjsBytes;

    private String summary;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
}
