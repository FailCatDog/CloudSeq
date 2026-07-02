package cn.guet.soft_manage.biz.workspace.service.impl;

import cn.guet.soft_manage.biz.workspace.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeCreateRequestDTO;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeRenameRequestDTO;
import cn.guet.soft_manage.biz.workspace.dto.WorkspaceNodeTreeDTO;
import cn.guet.soft_manage.biz.workspace.entity.Workspace;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceContent;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.document.service.DocumentManageService;
import cn.guet.soft_manage.biz.sheet.service.SheetManageService;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceAccessService;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceNodeService;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工作区节点服务实现
 */
@Service
public class WorkspaceNodeServiceImpl implements WorkspaceNodeService {

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private WorkspaceContentDao workspaceContentDao;

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private WorkspaceAccessService workspaceAccessService;

    @Resource
    private DocumentManageService documentManageService;

    @Resource
    private SheetManageService sheetManageService;

    @Override
    public List<WorkspaceNodeTreeDTO> getTree(Long workspaceId) {
        workspaceAccessService.requireCurrentAccess(workspaceId);

        List<WorkspaceNode> nodes = workspaceNodeDao.selectList(new LambdaQueryWrapper<WorkspaceNode>()
                .eq(WorkspaceNode::getWorkspaceId, workspaceId)
                .orderByAsc(WorkspaceNode::getSortOrder)
                .orderByAsc(WorkspaceNode::getId));

        List<WorkspaceNodeTreeDTO> tree = buildTree(nodes, loadContentMetadata(nodes));
        return flattenLegacyRoot(workspaceId, tree);
    }

    private Map<Long, WorkspaceContent> loadContentMetadata(List<WorkspaceNode> nodes) {
        List<Long> contentNodeIds = nodes.stream()
                .filter(node -> Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())
                        || Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode()))
                .map(WorkspaceNode::getId)
                .toList();
        if (contentNodeIds.isEmpty()) {
            return Map.of();
        }

        return workspaceContentDao.selectList(new LambdaQueryWrapper<WorkspaceContent>()
                        .in(WorkspaceContent::getNodeId, contentNodeIds)
                        .select(
                                WorkspaceContent::getNodeId,
                                WorkspaceContent::getSummary,
                                WorkspaceContent::getCharCount,
                                WorkspaceContent::getContentBytes,
                                WorkspaceContent::getUpdateDate
                        ))
                .stream()
                .collect(Collectors.toMap(WorkspaceContent::getNodeId, content -> content, (left, right) -> left));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkspaceNode createNode(WorkspaceNodeCreateRequestDTO request) {
        validateCreateRequest(request);
        workspaceAccessService.requireWritableWorkspace(request.getWorkspaceId());

        Long parentId = request.getParentId();
        if (parentId != null) {
            WorkspaceNode parent = requireNodeInWorkspace(parentId, request.getWorkspaceId());
            if (!Objects.equals(parent.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_FOLDER.getCode())) {
                throw new BusinessException(BizResponseCode.NODE_PARENT_MUST_BE_FOLDER);
            }
        }

        String nodeType = request.getNodeType();
        Long userId = UserContext.getUserId();

        WorkspaceNode node = WorkspaceNode.builder()
                .workspaceId(request.getWorkspaceId())
                .parentId(parentId)
                .nodeType(nodeType)
                .title(request.getTitle().trim())
                .sortOrder(nextSortOrder(request.getWorkspaceId(), parentId))
                .createUser(userId)
                .updateUser(userId)
                .build();
        workspaceNodeDao.insert(node);

        if (Objects.equals(nodeType, CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())
                || Objects.equals(nodeType, CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode())) {
            WorkspaceContent content = WorkspaceContent.builder()
                    .nodeId(node.getId())
                    .contentMd("")
                    .charCount(0)
                    .contentBytes(0)
                    .yjsBytes(0)
                    .summary(Objects.equals(nodeType, CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode()) ? "空表格" : "")
                    .createUser(userId)
                    .updateUser(userId)
                    .build();
            workspaceContentDao.insert(content);
        }

        return node;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkspaceNode renameNode(Long nodeId, WorkspaceNodeRenameRequestDTO request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(BizResponseCode.NODE_TITLE_REQUIRED);
        }

        WorkspaceNode node = requireExistingNode(nodeId);
        workspaceAccessService.requireWritableWorkspace(node.getWorkspaceId());

        node.setTitle(request.getTitle().trim());
        node.setUpdateUser(UserContext.getUserId());
        workspaceNodeDao.updateById(node);
        return node;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(Long nodeId) {
        WorkspaceNode node = requireExistingNode(nodeId);
        workspaceAccessService.requireWritableWorkspace(node.getWorkspaceId());

        Workspace workspace = workspaceDao.selectById(node.getWorkspaceId());
        if (workspace != null && Objects.equals(workspace.getRootNodeId(), nodeId)) {
            throw new BusinessException(BizResponseCode.ROOT_NODE_NOT_DELETABLE);
        }

        deleteNodeRecursive(node);
    }

    private void validateCreateRequest(WorkspaceNodeCreateRequestDTO request) {
        if (request.getWorkspaceId() == null) {
            throw new BusinessException(BizResponseCode.WORKSPACE_ID_REQUIRED);
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException(BizResponseCode.NODE_TITLE_REQUIRED);
        }

        if (!isSupportedNodeType(request.getNodeType())) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_CREATE_UNSUPPORTED);
        }
    }

    private boolean isSupportedNodeType(String nodeType) {
        return Objects.equals(nodeType, CacheCode.WORKSPACE_NODE_TYPE_FOLDER.getCode())
                || Objects.equals(nodeType, CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())
                || Objects.equals(nodeType, CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode());
    }

    /**
     * 兼容历史数据：旧版自动创建的根文件夹不在树中展示，其子节点提升为顶层。
     */
    private List<WorkspaceNodeTreeDTO> flattenLegacyRoot(Long workspaceId, List<WorkspaceNodeTreeDTO> tree) {
        Workspace workspace = workspaceDao.selectById(workspaceId);
        if (workspace == null || workspace.getRootNodeId() == null || tree.isEmpty()) {
            return tree;
        }

        Long rootId = workspace.getRootNodeId();
        List<WorkspaceNodeTreeDTO> flattened = new ArrayList<>();
        for (WorkspaceNodeTreeDTO node : tree) {
            if (Objects.equals(node.getId(), rootId)) {
                if (node.getChildren() != null) {
                    flattened.addAll(node.getChildren());
                }
            } else {
                flattened.add(node);
            }
        }
        sortTree(flattened);
        return flattened;
    }

    private void deleteNodeRecursive(WorkspaceNode node) {
        List<WorkspaceNode> children = workspaceNodeDao.selectList(new LambdaQueryWrapper<WorkspaceNode>()
                .eq(WorkspaceNode::getParentId, node.getId()));
        for (WorkspaceNode child : children) {
            deleteNodeRecursive(child);
        }

        if (Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) {
            documentManageService.cleanupByNodeId(node.getId());
        } else if (Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode())) {
            sheetManageService.cleanupByNodeId(node.getId());
        }
        workspaceNodeDao.deleteById(node.getId());
    }

    private List<WorkspaceNodeTreeDTO> buildTree(List<WorkspaceNode> nodes, Map<Long, WorkspaceContent> contentMetadata) {
        Map<Long, WorkspaceNodeTreeDTO> dtoMap = new HashMap<>();
        List<WorkspaceNodeTreeDTO> roots = new ArrayList<>();

        for (WorkspaceNode node : nodes) {
            WorkspaceNodeTreeDTO.WorkspaceNodeTreeDTOBuilder builder = WorkspaceNodeTreeDTO.builder()
                    .id(node.getId())
                    .parentId(node.getParentId())
                    .nodeType(node.getNodeType())
                    .title(node.getTitle())
                    .sortOrder(node.getSortOrder())
                    .children(new ArrayList<>());

            if (Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())
                    || Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_SHEET.getCode())) {
                WorkspaceContent content = contentMetadata.get(node.getId());
                if (content != null) {
                    builder.summary(content.getSummary() != null ? content.getSummary() : "")
                            .charCount(content.getCharCount() != null ? content.getCharCount() : 0)
                            .contentBytes(content.getContentBytes() != null ? content.getContentBytes() : 0)
                            .contentUpdateDate(content.getUpdateDate());
                } else {
                    builder.summary("")
                            .charCount(0)
                            .contentBytes(0);
                }
            }

            dtoMap.put(node.getId(), builder.build());
        }

        for (WorkspaceNode node : nodes) {
            WorkspaceNodeTreeDTO dto = dtoMap.get(node.getId());
            if (node.getParentId() == null) {
                roots.add(dto);
                continue;
            }
            WorkspaceNodeTreeDTO parent = dtoMap.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(dto);
            } else {
                roots.add(dto);
            }
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<WorkspaceNodeTreeDTO> nodes) {
        nodes.sort(Comparator
                .comparing(WorkspaceNodeTreeDTO::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(WorkspaceNodeTreeDTO::getId, Comparator.nullsLast(Long::compareTo)));
        for (WorkspaceNodeTreeDTO node : nodes) {
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                sortTree(node.getChildren());
            }
        }
    }

    private int nextSortOrder(Long workspaceId, Long parentId) {
        LambdaQueryWrapper<WorkspaceNode> wrapper = new LambdaQueryWrapper<WorkspaceNode>()
                .eq(WorkspaceNode::getWorkspaceId, workspaceId)
                .orderByDesc(WorkspaceNode::getSortOrder)
                .last("LIMIT 1");
        if (parentId == null) {
            wrapper.isNull(WorkspaceNode::getParentId);
        } else {
            wrapper.eq(WorkspaceNode::getParentId, parentId);
        }

        WorkspaceNode last = workspaceNodeDao.selectOne(wrapper);
        return last == null ? 0 : last.getSortOrder() + 1;
    }

    private WorkspaceNode requireExistingNode(Long nodeId) {
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) {
            throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        }
        return node;
    }

    private WorkspaceNode requireNodeInWorkspace(Long nodeId, Long workspaceId) {
        WorkspaceNode node = requireExistingNode(nodeId);
        if (!Objects.equals(node.getWorkspaceId(), workspaceId)) {
            throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        }
        return node;
    }
}
