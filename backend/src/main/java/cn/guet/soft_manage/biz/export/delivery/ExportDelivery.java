package cn.guet.soft_manage.biz.export.delivery;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;

/**
 * 导出产物交付（如直传、临时 URL 等）
 */
public interface ExportDelivery {

    ExportArtifact deliver(ExportArtifact artifact);
}
