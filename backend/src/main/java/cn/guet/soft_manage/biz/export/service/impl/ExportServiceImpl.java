package cn.guet.soft_manage.biz.export.service.impl;

import cn.guet.soft_manage.biz.export.asset.ExportAssetResolver;
import cn.guet.soft_manage.biz.export.delivery.ExportDelivery;
import cn.guet.soft_manage.biz.export.dto.ExportArtifact;
import cn.guet.soft_manage.biz.export.dto.ExportContext;
import cn.guet.soft_manage.biz.export.dto.ExportFormat;
import cn.guet.soft_manage.biz.export.exporter.FormatRegistry;
import cn.guet.soft_manage.biz.export.exporter.NodeExporter;
import cn.guet.soft_manage.biz.export.service.ExportService;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceAccessService;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

@Service
public class ExportServiceImpl implements ExportService {

    private final WorkspaceNodeDao workspaceNodeDao;
    private final WorkspaceContentDao workspaceContentDao;
    private final WorkspaceAccessService workspaceAccessService;
    private final FormatRegistry formatRegistry;
    private final ExportDelivery exportDelivery;
    private final ExportAssetResolver exportAssetResolver;

    public ExportServiceImpl(
            WorkspaceNodeDao workspaceNodeDao,
            WorkspaceContentDao workspaceContentDao,
            WorkspaceAccessService workspaceAccessService,
            FormatRegistry formatRegistry,
            ExportDelivery exportDelivery,
            ExportAssetResolver exportAssetResolver) {
        this.workspaceNodeDao = workspaceNodeDao;
        this.workspaceContentDao = workspaceContentDao;
        this.workspaceAccessService = workspaceAccessService;
        this.formatRegistry = formatRegistry;
        this.exportDelivery = exportDelivery;
        this.exportAssetResolver = exportAssetResolver;
    }

    @Override
    public ExportArtifact export(Long nodeId, String formatParam) {
        return export(nodeId, formatParam, null);
    }

    @Override
    public ExportArtifact export(Long nodeId, String formatParam, String contentOverride) {
        ExportFormat format = ExportFormat.fromParam(formatParam);
        if (format == null) {
            throw new BusinessException(BizResponseCode.EXPORT_FORMAT_INVALID);
        }

        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) {
            throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        }

        workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) {
            throw new BusinessException(BizResponseCode.DOCUMENT_CONTENT_NOT_FOUND);
        }

        String contentMd = (contentOverride != null && !contentOverride.isBlank())
                ? contentOverride
                : (content.getContentMd() != null ? content.getContentMd() : "");

        ExportContext context = ExportContext.builder()
                .nodeId(node.getId())
                .workspaceId(node.getWorkspaceId())
                .nodeType(node.getNodeType())
                .title(node.getTitle())
                .contentMd(contentMd)
                .assetResolver(exportAssetResolver)
                .build();

        NodeExporter exporter = formatRegistry.resolve(node.getNodeType(), format);
        ExportArtifact artifact = exporter.export(context);
        return exportDelivery.deliver(artifact);
    }
}
