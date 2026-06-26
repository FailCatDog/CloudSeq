package cn.guet.soft_manage.biz.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Office 文档详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeDocumentDetailDTO {

    private Long nodeId;

    private Long workspaceId;

    private String title;

    private String fileName;

    private Long version;

    private Boolean canWrite;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
}
