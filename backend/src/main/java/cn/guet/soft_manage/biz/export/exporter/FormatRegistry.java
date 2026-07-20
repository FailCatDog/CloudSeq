package cn.guet.soft_manage.biz.export.exporter;

import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FormatRegistry {
    private final List<NodeExporter> exporters;

    public FormatRegistry(List<NodeExporter> exporters) {
        this.exporters = exporters == null ? List.of() : List.copyOf(exporters);
    }

    public NodeExporter resolve(String nodeType, ExportFormat format) {
        if (nodeType == null || format == null) {
            throw new BusinessException(BizResponseCode.EXPORT_FORMAT_INVALID);
        }
        return exporters.stream()
            .filter(e -> e.supports(nodeType, format))
            .findFirst()
            .orElseThrow(() -> new BusinessException(BizResponseCode.EXPORT_FORMAT_INVALID));
    }
}
