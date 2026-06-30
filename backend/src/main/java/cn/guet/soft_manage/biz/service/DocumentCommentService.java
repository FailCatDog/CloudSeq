package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.DocumentCommentCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentCommentDTO;

import java.util.List;

/**
 * 文档评论服务
 */
public interface DocumentCommentService {

    List<DocumentCommentDTO> listByNodeId(Long nodeId);

    DocumentCommentDTO create(Long nodeId, DocumentCommentCreateRequestDTO request);

    void delete(Long commentId);

    void deleteCommentsByNodeId(Long nodeId);
}
