package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.entity.DocumentEdit;
import cn.guet.feishu.entity.GroupDocument;
import cn.guet.feishu.mapper.DocumentEditMapper;
import cn.guet.feishu.mapper.GroupDocumentMapper;
import cn.guet.feishu.service.DocumentEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentEditServiceImpl implements DocumentEditService {

    private final DocumentEditMapper documentEditMapper;
    private final GroupDocumentMapper groupDocumentMapper;

    @Override
    public DocumentEdit getDocumentEdit(String documentId) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        DocumentEdit edit = documentEditMapper.findByDocumentId(documentId);
        if (edit == null) {
            edit = new DocumentEdit();
            edit.setEditId(UUID.randomUUID().toString());
            edit.setDocumentId(documentId);
            edit.setContent("");
            edit.setStatus(1);
            documentEditMapper.insert(edit);
        }
        return edit;
    }

    @Override
    @Transactional
    public DocumentEdit startEdit(String userId, String documentId) {
        DocumentEdit edit = getDocumentEdit(documentId);
        
        if (edit.getEditingUserId() != null && !edit.getEditingUserId().equals(userId)) {
            LocalDateTime editingTime = edit.getEditingStartTime();
            if (editingTime != null && editingTime.plusMinutes(30).isAfter(LocalDateTime.now())) {
                throw new BusinessException("文档正在被其他用户编辑");
            }
        }

        edit.setEditingUserId(userId);
        edit.setEditingStartTime(LocalDateTime.now());
        documentEditMapper.update(edit);
        return edit;
    }

    @Override
    @Transactional
    public void saveContent(String documentId, String content) {
        documentEditMapper.updateContent(documentId, content);
    }

    @Override
    @Transactional
    public void releaseEdit(String userId, String documentId) {
        DocumentEdit edit = documentEditMapper.findByDocumentId(documentId);
        if (edit != null && userId.equals(edit.getEditingUserId())) {
            documentEditMapper.releaseEditLock(documentId);
        }
    }

    @Override
    @Transactional
    public void heartbeat(String userId, String documentId) {
        DocumentEdit edit = documentEditMapper.findByDocumentId(documentId);
        if (edit != null && userId.equals(edit.getEditingUserId())) {
            documentEditMapper.updateEditingTime(documentId);
        }
    }
}

