package cn.guet.soft_manage.biz.sheet.service.impl;

import cn.guet.soft_manage.biz.document.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.document.dto.CollabPersistResponseDTO;
import cn.guet.soft_manage.biz.document.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.sheet.dto.SheetDetailDTO;
import cn.guet.soft_manage.biz.sheet.service.SheetService;
import cn.guet.soft_manage.biz.sheet.util.SheetContentMetadataUtil;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceAccessContext;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceAccessService;
import cn.guet.soft_manage.biz.document.util.CollabTokenUtil;
import cn.guet.soft_manage.frame.auth.LoginUser;
import cn.guet.soft_manage.frame.auth.UserContext;
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

@Service
public class SheetServiceImpl implements SheetService {

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
    public SheetDetailDTO getSheet(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        WorkspaceNode node = requireSheetNode(nodeId);
        WorkspaceAccessContext access = workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        WorkspaceContent content = requireContent(nodeId);
        return toSheetDetail(node, content, access.isCanWrite());
    }

    @Override
    public CollabTokenResponseDTO issueCollabToken(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        WorkspaceNode node = requireSheetNode(nodeId);
        WorkspaceAccessContext access = workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        Long userId = UserContext.getUserId();
        if (userId == null) throw new BusinessException(BizResponseCode.UNAUTHORIZED);

        LoginUser loginUser = UserContext.get();
        String displayName = loginUser != null ? loginUser.displayName() : "用户";
        String avatarUrl = loginUser != null ? loginUser.getAvatarUrl() : null;

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
    public CollabLoadResponseDTO loadForCollab(Long nodeId) {
        if (nodeId == null) throw new BusinessException(BizResponseCode.PARAM_ERROR);

        requireSheetNode(nodeId);

        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) {
            content = WorkspaceContent.builder()
                    .nodeId(nodeId)
                    .contentMd("")
                    .charCount(0)
                    .contentBytes(0)
                    .yjsBytes(0)
                    .summary("空表格")
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
        requireSheetNode(nodeId);

        WorkspaceContent content = requireContent(nodeId);
        if (!Objects.equals(content.getVersion(), request.getVersion())) {
            throw new BusinessException(BizResponseCode.DOCUMENT_STALE);
        }

        content.setYjsState(Base64.getDecoder().decode(request.getYjsStateBase64()));
        if (StringUtils.hasText(request.getContentMd())) {
            content.setContentMd(request.getContentMd());
        }
        content.setUpdateUser(request.getUpdateUser() != null ? request.getUpdateUser() : 0L);
        SheetContentMetadataUtil.applyTo(content);

        int rows = workspaceContentDao.updateById(content);
        if (rows == 0) throw new BusinessException(BizResponseCode.DOCUMENT_STALE);

        WorkspaceContent saved = workspaceContentDao.selectById(content.getId());
        return CollabPersistResponseDTO.builder()
                .nodeId(nodeId)
                .version(saved.getVersion())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContentByNodeId(Long nodeId) {
        if (nodeId == null) return;
        workspaceContentDao.delete(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
    }

    private WorkspaceNode requireSheetNode(Long nodeId) {
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode())) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);
        }
        return node;
    }

    private WorkspaceContent requireContent(Long nodeId) {
        WorkspaceContent content = workspaceContentDao.selectOne(new LambdaQueryWrapper<WorkspaceContent>()
                .eq(WorkspaceContent::getNodeId, nodeId));
        if (content == null) throw new BusinessException(BizResponseCode.DOCUMENT_CONTENT_NOT_FOUND);
        return content;
    }

    private SheetDetailDTO toSheetDetail(WorkspaceNode node, WorkspaceContent content, boolean canWrite) {
        return SheetDetailDTO.builder()
                .nodeId(node.getId())
                .workspaceId(node.getWorkspaceId())
                .title(node.getTitle())
                .contentJson(content.getContentMd() != null ? content.getContentMd() : "")
                .version(content.getVersion())
                .canWrite(canWrite)
                .cellCount(content.getCharCount() != null ? content.getCharCount() : 0)
                .contentBytes(content.getContentBytes() != null ? content.getContentBytes() : 0)
                .yjsBytes(content.getYjsBytes() != null ? content.getYjsBytes() : 0)
                .summary(content.getSummary() != null ? content.getSummary() : "")
                .updateDate(content.getUpdateDate())
                .build();
    }
}
