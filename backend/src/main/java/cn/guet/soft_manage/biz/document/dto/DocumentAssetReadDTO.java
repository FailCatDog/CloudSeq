package cn.guet.soft_manage.biz.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * 文档资产读取结果（供 Controller 流式输出）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentAssetReadDTO {

    private InputStream inputStream;

    private String contentType;

    private String fileName;

    private Long sizeBytes;
}
