package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;

/**
 * 按节点类型与格式导出
 */
public interface NodeExporter {

    boolean supports(String nodeType, ExportFormat format);

    ExportArtifact export(ExportContext ctx);
}
