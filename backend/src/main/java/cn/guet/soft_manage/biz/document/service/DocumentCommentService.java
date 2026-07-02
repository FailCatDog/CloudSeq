package cn.guet.soft_manage.biz.document.service;

import cn.guet.soft_manage.biz.document.dto.DocumentCommentCreateRequestDTO;
import cn.guet.soft_manage.biz.document.dto.DocumentCommentDTO;

import java.util.List;

/**
 * 文档评论服务
 */
public interface DocumentCommentService {

    /**
     * 查询文档下全部评论
     * @param nodeId 节点ID
     * @return 评论列表
     */
    List<DocumentCommentDTO> listByNodeId(Long nodeId);

    /**
     * 创建文档评论
     * @param nodeId 节点ID
     * @param request 创建请求
     * @return 新建评论
     */
    DocumentCommentDTO create(Long nodeId, DocumentCommentCreateRequestDTO request);

    /**
     * 删除单条评论
     * @param commentId 评论ID
     */
    void delete(Long commentId);

    /**
     * 删除文档节点下全部评论
     * @param nodeId 节点ID
     */
    void deleteCommentsByNodeId(Long nodeId);
}
