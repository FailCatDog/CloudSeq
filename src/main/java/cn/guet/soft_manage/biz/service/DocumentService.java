package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentDetailDTO;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: Markdown 文档服务
 */
public interface DocumentService {

    DocumentDetailDTO getDocument(Long nodeId);

    CollabTokenResponseDTO issueCollabToken(Long nodeId);

    DocumentDetailDTO saveContent(Long nodeId, DocumentContentSaveRequestDTO request);

    CollabLoadResponseDTO loadForCollab(Long nodeId);

    void persistFromCollab(CollabPersistRequestDTO request);
}
