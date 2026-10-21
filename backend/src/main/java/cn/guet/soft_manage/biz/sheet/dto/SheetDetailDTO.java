package cn.guet.soft_manage.biz.sheet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作区表格详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SheetDetailDTO {

    private Long nodeId;

    private Long workspaceId;

    private String title;

    /** WorkbookSnapshot JSON */
    private String contentJson;

    private Long version;

    private Boolean canWrite;

    private Integer cellCount;

    private Integer contentBytes;

    private Integer yjsBytes;

    private String summary;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateDate;
}
