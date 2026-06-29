package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.pojo.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabPersistResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentContentSaveRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentDetailDTO;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceContent;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.service.DocumentService;
import cn.guet.soft_manage.biz.service.WorkspaceAccessService;
import cn.guet.soft_manage.biz.utils.CollabTokenUtil;
import cn.guet.soft_manage.biz.utils.WorkspaceContentMetadataUtil;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.config.CollabProperties;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.Objects;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区文档服务实现
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private WorkspaceContentDao workspaceContentDao;

    @Resource
    private WorkspaceAccessService workspaceAccessService;

    @Resource
    private CollabTokenUtil collabTokenUtil;

    @Resource
    private CollabProperties collabProperties;

    @Override
    public DocumentDetailDTO getDocument(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);

        WorkspaceAccessContext access = workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) throw new BusinessException(BizResponseCode.DOCUMENT_CONTENT_NOT_FOUND);

        return toDocumentDetail(node, content, access.isCanWrite());
    }

    private DocumentDetailDTO toDocumentDetail(WorkspaceNode node, WorkspaceContent content, boolean canWrite) {
        return DocumentDetailDTO.builder()
                .nodeId(node.getId())
                .workspaceId(node.getWorkspaceId())
                .title(node.getTitle())
                .contentMd(content.getContentMd() != null ? content.getContentMd() : "")
                .version(content.getVersion())
                .canWrite(canWrite)
                .charCount(content.getCharCount() != null ? content.getCharCount() : 0)
                .contentBytes(content.getContentBytes() != null ? content.getContentBytes() : 0)
                .yjsBytes(content.getYjsBytes() != null ? content.getYjsBytes() : 0)
                .summary(content.getSummary() != null ? content.getSummary() : "")
                .updateDate(content.getUpdateDate())
                .build();
    }

    @Override
    public CollabTokenResponseDTO issueCollabToken(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);

        WorkspaceAccessContext access = workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(BizResponseCode.UNAUTHORIZED);

        User user = UserContext.get();
        String displayName = user != null && StringUtils.hasText(user.getRealName()) ? user.getRealName()
                : (user != null && StringUtils.hasText(user.getNickName()) ? user.getNickName()
                : (user != null && StringUtils.hasText(user.getUsername()) ? user.getUsername() : "用户"));
        String avatarUrl = user != null ? user.getAvatarUrl() : null;

        String token = collabTokenUtil.generateToken(userId, nodeId, access.isCanWrite(), displayName, avatarUrl);

        return CollabTokenResponseDTO.builder()
                .token(token)
                .wsUrl(collabProperties.getWsUrl())
                .room(collabProperties.getRoomPrefix() + nodeId)
                .canWrite(access.isCanWrite())
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentDetailDTO saveContent(Long nodeId, DocumentContentSaveRequestDTO request) {
        if (nodeId == null || request == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);
        if (request.getVersion() == null) throw new BusinessException(BizResponseCode.DOCUMENT_VERSION_REQUIRED);

        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);

        workspaceAccessService.requireWritableWorkspace(node.getWorkspaceId());

        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) throw new BusinessException(BizResponseCode.DOCUMENT_CONTENT_NOT_FOUND);
        if (!Objects.equals(content.getVersion(), request.getVersion())) throw new BusinessException(BizResponseCode.DOCUMENT_STALE);

        content.setContentMd(request.getContentMd() != null ? request.getContentMd() : "");
        content.setYjsState(null);
        content.setUpdateUser(UserContext.getUserId());
        WorkspaceContentMetadataUtil.applyTo(content);
        int rows = workspaceContentDao.updateById(content);
        if (rows == 0) throw new BusinessException(BizResponseCode.DOCUMENT_STALE);

        WorkspaceContent saved = workspaceContentDao.selectById(content.getId());
        return toDocumentDetail(node, saved, true);
    }

    @Override
    public CollabLoadResponseDTO loadForCollab(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);

        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) {
            content = WorkspaceContent.builder()
                    .nodeId(nodeId)
                    .contentMd("")
                    .charCount(0)
                    .contentBytes(0)
                    .yjsBytes(0)
                    .summary("")
                    .createUser(0L)
                    .updateUser(0L)
                    .build();
            workspaceContentDao.insert(content);
        }

        String yjsStateBase64 = content.getYjsState() != null && content.getYjsState().length > 0
                ? Base64.getEncoder().encodeToString(content.getYjsState())
                : null;

        return CollabLoadResponseDTO.builder()
                .nodeId(nodeId)
                .contentMd(content.getContentMd() != null ? content.getContentMd() : "")
                .yjsStateBase64(yjsStateBase64)
                .version(content.getVersion())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CollabPersistResponseDTO persistFromCollab(CollabPersistRequestDTO request) {
        if (request == null || request.getNodeId() == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);
        if (request.getVersion() == null) throw new BusinessException(BizResponseCode.DOCUMENT_VERSION_REQUIRED);
        if (!StringUtils.hasText(request.getYjsStateBase64())) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        Long nodeId = request.getNodeId();
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);

        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) throw new BusinessException(BizResponseCode.DOCUMENT_CONTENT_NOT_FOUND);
        if (!Objects.equals(content.getVersion(), request.getVersion())) throw new BusinessException(BizResponseCode.DOCUMENT_STALE);

        content.setYjsState(Base64.getDecoder().decode(request.getYjsStateBase64()));
        if (StringUtils.hasText(request.getContentMd())) {
            content.setContentMd(request.getContentMd());
        }
        content.setUpdateUser(request.getUpdateUser() != null ? request.getUpdateUser() : 0L);
        WorkspaceContentMetadataUtil.applyTo(content);

        int rows = workspaceContentDao.updateById(content);
        if (rows == 0) throw new BusinessException(BizResponseCode.DOCUMENT_STALE);

        WorkspaceContent saved = workspaceContentDao.selectById(content.getId());
        return CollabPersistResponseDTO.builder()
                .nodeId(nodeId)
                .version(saved.getVersion())
                .build();
    }
}
