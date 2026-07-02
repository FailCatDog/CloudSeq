package cn.guet.soft_manage.biz.document.service.impl;

import cn.guet.soft_manage.biz.document.service.DocumentAssetService;
import cn.guet.soft_manage.biz.document.service.DocumentCommentService;
import cn.guet.soft_manage.biz.document.service.DocumentManageService;
import cn.guet.soft_manage.biz.document.service.DocumentService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentManageServiceImpl implements DocumentManageService {

    @Resource
    private DocumentService documentService;

    @Resource
    private DocumentCommentService documentCommentService;

    @Resource
    private ObjectProvider<DocumentAssetService> documentAssetServiceProvider;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanupByNodeId(Long nodeId) {
        if (nodeId == null) return;

        DocumentAssetService documentAssetService = documentAssetServiceProvider.getIfAvailable();
        if (documentAssetService != null) documentAssetService.deleteAssetsByNodeId(nodeId);

        documentCommentService.deleteCommentsByNodeId(nodeId);
        documentService.deleteContentByNodeId(nodeId);
    }
}
