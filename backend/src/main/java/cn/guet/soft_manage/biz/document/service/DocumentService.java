package cn.guet.soft_manage.biz.document.service;

import cn.guet.soft_manage.biz.document.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.document.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.document.dto.DocumentDetailDTO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: Markdown 文档服务
 */
public interface DocumentService {

    /**
     * 根据节点ID获取文档
     * @param nodeId 节点ID
     * @return 文档详情
     */
    DocumentDetailDTO getDocument(Long nodeId);

    /**
     * 签发协同编辑令牌
     * @param nodeId 节点ID
     * @return 协同令牌信息
     */
    CollabTokenResponseDTO issueCollabToken(Long nodeId);

    /**
     * 保存文档正文
     * @param nodeId 节点ID
     * @param request 保存请求
     * @return 保存后的文档详情
     */
    DocumentDetailDTO saveContent(Long nodeId, DocumentContentSaveRequestDTO request);

    /**
     * 协同服务加载文档
     * @param nodeId 节点ID
     * @return 协同加载数据
     */
    CollabLoadResponseDTO loadForCollab(Long nodeId);

    /**
     * 协同服务持久化文档
     * @param request 持久化请求
     * @return 持久化结果
     */
    CollabPersistResponseDTO persistFromCollab(CollabPersistRequestDTO request);

    /**
     * 删除文档正文
     * @param nodeId 节点ID
     */
    void deleteContentByNodeId(Long nodeId);
}
