package cn.guet.soft_manage.frame.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 工作区节点导出限制
 */
@Data
@Component
@ConfigurationProperties(prefix = "export")
public class ExportProperties {

    /** 文档类导出最大字符数 */
    private int maxDocumentChars = 100_000;

    /** 表格类导出最大单元格数 */
    private int maxSheetCells = 50_000;

    /** 内嵌图片总大小上限（字节） */
    private long maxEmbeddedImageBytes = 20L * 1024 * 1024;
}
