package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * OnlyOffice 编辑器配置（供前端 DocsAPI 初始化）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeEditorConfigDTO {

    private String documentServerUrl;

    private Map<String, Object> config;
}
