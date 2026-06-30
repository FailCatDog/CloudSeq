package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.DocumentCommentDao;
import cn.guet.soft_manage.biz.dao.UserDao;
import cn.guet.soft_manage.biz.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.pojo.dto.DocumentCommentCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.DocumentCommentDTO;
import cn.guet.soft_manage.biz.pojo.entity.DocumentComment;
import cn.guet.soft_manage.biz.pojo.entity.User;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.service.DocumentCommentService;
import cn.guet.soft_manage.biz.service.WorkspaceAccessService;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.constant.DocumentCommentConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentCommentServiceImpl implements DocumentCommentService {

    @Resource
    private DocumentCommentDao documentCommentDao;

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private UserDao userDao;

    @Resource
    private WorkspaceAccessService workspaceAccessService;

    @Override
    public List<DocumentCommentDTO> listByNodeId(Long nodeId) {
        WorkspaceNode node = requireDocumentNode(nodeId);
        workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        List<DocumentComment> comments = documentCommentDao.selectList(new LambdaQueryWrapper<DocumentComment>()
                .eq(DocumentComment::getNodeId, nodeId)
                .orderByAsc(DocumentComment::getAnchorPos)
                .orderByAsc(DocumentComment::getCreateDate));
        if (comments.isEmpty()) {
            return List.of();
        }

        Map<Long, User> authorMap = loadAuthors(comments);
        return comments.stream()
                .map(comment -> toDTO(comment, authorMap.get(comment.getCreateUser())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentCommentDTO create(Long nodeId, DocumentCommentCreateRequestDTO request) {
        if (request == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }

        WorkspaceNode node = requireDocumentNode(nodeId);
        workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }

        validateCreateRequest(request);

        DocumentComment comment = DocumentComment.builder()
                .workspaceId(node.getWorkspaceId())
                .nodeId(nodeId)
                .anchorType(DocumentCommentConstants.ANCHOR_TYPE_BLOCK)
                .anchorPos(request.getAnchorPos())
                .quoteText(trimQuoteText(request.getQuoteText()))
                .content(request.getContent().trim())
                .build();

        documentCommentDao.insert(comment);

        User author = userDao.selectById(userId);
        return toDTO(comment, author);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long commentId) {
        if (commentId == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }

        DocumentComment comment = documentCommentDao.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(BizResponseCode.DOCUMENT_COMMENT_NOT_FOUND);
        }

        WorkspaceNode node = requireDocumentNode(comment.getNodeId());
        workspaceAccessService.requireCurrentAccess(node.getWorkspaceId());

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(BizResponseCode.UNAUTHORIZED);
        }
        if (!Objects.equals(comment.getCreateUser(), userId)) {
            throw new BusinessException(BizResponseCode.FORBIDDEN);
        }

        documentCommentDao.deleteById(commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentsByNodeId(Long nodeId) {
        if (nodeId == null) {
            return;
        }

        documentCommentDao.delete(new LambdaQueryWrapper<DocumentComment>()
                .eq(DocumentComment::getNodeId, nodeId));
    }

    private WorkspaceNode requireDocumentNode(Long nodeId) {
        if (nodeId == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }

        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) {
            throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        }
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);
        }
        return node;
    }

    private void validateCreateRequest(DocumentCommentCreateRequestDTO request) {
        if (request.getAnchorPos() == null || request.getAnchorPos() < 0) {
            throw new BusinessException(BizResponseCode.DOCUMENT_COMMENT_ANCHOR_REQUIRED);
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new BusinessException(BizResponseCode.DOCUMENT_COMMENT_CONTENT_REQUIRED);
        }

        String content = request.getContent().trim();
        if (content.length() > DocumentCommentConstants.CONTENT_MAX_LENGTH) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }
    }

    private static String trimQuoteText(String quoteText) {
        if (!StringUtils.hasText(quoteText)) {
            return null;
        }
        String trimmed = quoteText.trim();
        if (trimmed.length() <= DocumentCommentConstants.QUOTE_TEXT_MAX_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, DocumentCommentConstants.QUOTE_TEXT_MAX_LENGTH);
    }

    private Map<Long, User> loadAuthors(List<DocumentComment> comments) {
        Set<Long> authorIds = comments.stream()
                .map(DocumentComment::getCreateUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userDao.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, authorIds))
                .stream()
                .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left));
    }

    private DocumentCommentDTO toDTO(DocumentComment comment, User author) {
        return DocumentCommentDTO.builder()
                .id(comment.getId())
                .nodeId(comment.getNodeId())
                .workspaceId(comment.getWorkspaceId())
                .anchorType(comment.getAnchorType())
                .anchorPos(comment.getAnchorPos())
                .anchorTo(comment.getAnchorTo())
                .quoteText(comment.getQuoteText())
                .content(comment.getContent())
                .threadId(comment.getThreadId())
                .parentCommentId(comment.getParentCommentId())
                .commentStatus(comment.getCommentStatus())
                .authorId(comment.getCreateUser())
                .authorName(resolveDisplayName(author))
                .authorAvatarUrl(author != null ? author.getAvatarUrl() : null)
                .createDate(comment.getCreateDate())
                .build();
    }

    private static String resolveDisplayName(User user) {
        if (user == null) {
            return "用户";
        }
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        if (StringUtils.hasText(user.getNickName())) {
            return user.getNickName();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "用户";
    }
}
