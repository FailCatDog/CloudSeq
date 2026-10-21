package cn.guet.soft_manage.biz.export.service;

import cn.guet.soft_manage.biz.export.dto.ExportArtifact;

/**
 * 节点导出服务
 */
public interface ExportService {

    ExportArtifact export(Long nodeId, String formatParam);

    /**
     * @param contentOverride 可选：编辑器当前正文/快照；非空时优先于库中 contentMd（避免协同落库延迟导致空导出）
     */
    ExportArtifact export(Long nodeId, String formatParam, String contentOverride);
}
