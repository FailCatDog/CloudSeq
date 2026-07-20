package cn.guet.soft_manage.biz.export.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出产物（内存字节 + MIME + 文件名）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportArtifact {

    private byte[] bytes;

    private String contentType;

    private String fileName;
}
