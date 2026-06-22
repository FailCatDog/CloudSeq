package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.pojo.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentDetailDTO;
import cn.guet.soft_manage.biz.service.DocumentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: Markdown 文档服务实现
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private WorkspaceContentDao workspaceContentDao;

    @Override
    public DocumentDetailDTO getDocument(Long nodeId) {
        return null;
    }

    @Override
    public CollabTokenResponseDTO issueCollabToken(Long nodeId) {
        return null;
    }

    @Override
    public DocumentDetailDTO saveContent(Long nodeId, DocumentContentSaveRequestDTO request) {
        return null;
    }

    @Override
    public CollabLoadResponseDTO loadForCollab(Long nodeId) {
        return null;
    }

    @Override
    public void persistFromCollab(CollabPersistRequestDTO request) {
    }
}
