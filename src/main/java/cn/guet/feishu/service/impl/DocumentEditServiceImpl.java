package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.controller.dto.DocumentEditStatusDTO;
import cn.guet.feishu.entity.DocumentEdit;
import cn.guet.feishu.entity.GroupDocument;
import cn.guet.feishu.entity.User;
import cn.guet.feishu.mapper.DocumentEditMapper;
import cn.guet.feishu.mapper.GroupDocumentMapper;
import cn.guet.feishu.mapper.UserMapper;
import cn.guet.feishu.service.DocumentEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentEditServiceImpl implements DocumentEditService {

    private static final int LOCK_EXPIRE_MINUTES = 30;

    private final DocumentEditMapper documentEditMapper;
    private final GroupDocumentMapper groupDocumentMapper;
    private final UserMapper userMapper;

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
    public DocumentEditStatusDTO getEditStatus(String documentId) {
        DocumentEdit edit = getDocumentEdit(documentId);
        DocumentEditStatusDTO statusDTO = new DocumentEditStatusDTO();
        statusDTO.setDocumentId(documentId);
        statusDTO.setLocked(edit.getEditingUserId() != null);
        statusDTO.setEditingUserId(edit.getEditingUserId());
        statusDTO.setEditingStartTime(edit.getEditingStartTime());
        statusDTO.setExpired(isLockExpired(edit));

        if (edit.getEditingUserId() != null) {
            User user = userMapper.selectByUserId(edit.getEditingUserId());
            if (user != null) {
                statusDTO.setEditingUsername(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
        }
        return statusDTO;
    }

    @Override
    @Transactional
    public DocumentEdit startEdit(String userId, String documentId) {
        DocumentEdit edit = getDocumentEdit(documentId);
        releaseExpiredLock(edit);

        if (edit.getEditingUserId() != null && !edit.getEditingUserId().equals(userId)) {
            throw new BusinessException("文档正在被其他用户编辑");
        }

        edit.setEditingUserId(userId);
        edit.setEditingStartTime(LocalDateTime.now());
        documentEditMapper.update(edit);
        return edit;
    }

    @Override
    @Transactional
    public void saveContent(String userId, String documentId, String content) {
        DocumentEdit edit = getDocumentEdit(documentId);
        releaseExpiredLock(edit);

        if (edit.getEditingUserId() == null || !userId.equals(edit.getEditingUserId())) {
            throw new BusinessException("只有当前编辑者可以保存文档");
        }

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

    private void releaseExpiredLock(DocumentEdit edit) {
        if (isLockExpired(edit)) {
            documentEditMapper.releaseEditLock(edit.getDocumentId());
            edit.setEditingUserId(null);
            edit.setEditingStartTime(null);
        }
    }

    private boolean isLockExpired(DocumentEdit edit) {
        return edit.getEditingUserId() != null
                && edit.getEditingStartTime() != null
                && edit.getEditingStartTime().plusMinutes(LOCK_EXPIRE_MINUTES).isBefore(LocalDateTime.now());
    }
}

