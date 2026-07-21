package cn.guet.soft_manage.biz.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出时可附带编辑器当前正文/快照，避免协同落库延迟导致空文件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportContentRequestDTO {

    private String contentMd;
}
