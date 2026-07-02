package cn.guet.soft_manage.biz.document.service;

/**
 * 文档域编排服务：跨 DocumentService / DocumentCommentService / DocumentAssetService 的协作入口。
 */
public interface DocumentManageService {

    /**
     * 清理文档节点关联的全部数据
     * @param nodeId 节点ID
     */
    void cleanupByNodeId(Long nodeId);
}
