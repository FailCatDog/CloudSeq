package cn.guet.soft_manage.biz.export.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 节点导出格式
 */
@Getter
@RequiredArgsConstructor
public enum ExportFormat {

    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("pdf", "application/pdf"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final String param;
    private final String contentType;

    public static ExportFormat fromParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (ExportFormat format : values()) {
            if (format.param.equalsIgnoreCase(raw.trim())) {
                return format;
            }
        }
        return null;
    }
}
