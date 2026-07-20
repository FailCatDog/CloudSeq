package cn.guet.soft_manage.biz.export.service;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;

/**
 * 节点导出服务
 */
public interface ExportService {

    ExportArtifact export(Long nodeId, String formatParam);
}
