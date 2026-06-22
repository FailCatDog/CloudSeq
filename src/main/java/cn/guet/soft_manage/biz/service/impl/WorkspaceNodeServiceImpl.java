package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WorkspaceContentDao;
import cn.guet.soft_manage.biz.dao.WorkspaceDao;
import cn.guet.soft_manage.biz.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceNodeCreateRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceNodeRenameRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.WorkspaceNodeTreeDTO;
import cn.guet.soft_manage.biz.pojo.entity.Workspace;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceContent;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.service.WorkspaceAccessService;
import cn.guet.soft_manage.biz.service.WorkspaceNodeService;
import cn.guet.soft_manage.frame.common.UserContext;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.WorkspaceNodeType;
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

/**
 * 工作区节点服务实现
 */
@Service
public class WorkspaceNodeServiceImpl implements WorkspaceNodeService {

    private static final String DEFAULT_ROOT_TITLE = "项目文档";

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private WorkspaceContentDao workspaceContentDao;

    @Resource
    private WorkspaceDao workspaceDao;

    @Resource
    private WorkspaceAccessService workspaceAccessService;

    @Override
    public List<WorkspaceNodeTreeDTO> getTree(Long workspaceId) {
        workspaceAccessService.requireCurrentAccess(workspaceId);
        ensureRootNode(workspaceId);

        List<WorkspaceNode> nodes = workspaceNodeDao.selectList(new LambdaQueryWrapper<WorkspaceNode>()
                .eq(WorkspaceNode::getWorkspaceId, workspaceId)
                .eq(WorkspaceNode::getDelFlag, 0)
                .orderByAsc(WorkspaceNode::getSortOrder)
                .orderByAsc(WorkspaceNode::getId));

        return buildTree(nodes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkspaceNode createNode(WorkspaceNodeCreateRequestDTO request) {
        validateCreateRequest(request);
        workspaceAccessService.requireWritableWorkspace(request.getWorkspaceId());
        WorkspaceNode root = ensureRootNode(request.getWorkspaceId());

        Long parentId = resolveParentId(request.getParentId(), root.getId());
        WorkspaceNode parent = requireNodeInWorkspace(parentId, request.getWorkspaceId());
        if (!Objects.equals(parent.getNodeType(), WorkspaceNodeType.FOLDER.getCode())) {
            throw new BusinessException(BizResponseCode.NODE_PARENT_MUST_BE_FOLDER);
        }

        WorkspaceNodeType nodeType = WorkspaceNodeType.of(request.getNodeType());
        Long userId = UserContext.getUserId();

        WorkspaceNode node = WorkspaceNode.builder()
                .workspaceId(request.getWorkspaceId())
                .parentId(parentId)
                .nodeType(nodeType.getCode())
                .title(request.getTitle().trim())
                .sortOrder(nextSortOrder(request.getWorkspaceId(), parentId))
                .createUser(userId)
                .updateUser(userId)
                .build();
        workspaceNodeDao.insert(node);

        if (nodeType == WorkspaceNodeType.MARKDOWN) {
            WorkspaceContent content = WorkspaceContent.builder()
                    .nodeId(node.getId())
                    .contentMd("")
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

        WorkspaceNodeType nodeType = WorkspaceNodeType.of(request.getNodeType());
        if (nodeType == null || nodeType == WorkspaceNodeType.OFFICE) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_CREATE_UNSUPPORTED);
        }
    }

    private Long resolveParentId(Long parentId, Long rootNodeId) {
        return parentId == null ? rootNodeId : parentId;
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkspaceNode ensureRootNode(Long workspaceId) {
        Workspace workspace = workspaceDao.selectById(workspaceId);
        if (workspace == null || Objects.equals(workspace.getDelFlag(), 1)) {
            throw new BusinessException(BizResponseCode.WORKSPACE_NOT_FOUND);
        }

        if (workspace.getRootNodeId() != null) {
            WorkspaceNode root = workspaceNodeDao.selectById(workspace.getRootNodeId());
            if (root != null && !Objects.equals(root.getDelFlag(), 1)) {
                return root;
            }
        }

        Long userId = UserContext.getUserId();
        WorkspaceNode root = WorkspaceNode.builder()
                .workspaceId(workspaceId)
                .parentId(null)
                .nodeType(WorkspaceNodeType.FOLDER.getCode())
                .title(DEFAULT_ROOT_TITLE)
                .sortOrder(0)
                .createUser(userId)
                .updateUser(userId)
                .build();
        workspaceNodeDao.insert(root);

        workspace.setRootNodeId(root.getId());
        workspace.setUpdateUser(userId);
        workspaceDao.updateById(workspace);
        return root;
    }

    private void deleteNodeRecursive(WorkspaceNode node) {
        List<WorkspaceNode> children = workspaceNodeDao.selectList(new LambdaQueryWrapper<WorkspaceNode>()
                .eq(WorkspaceNode::getParentId, node.getId())
                .eq(WorkspaceNode::getDelFlag, 0));
        for (WorkspaceNode child : children) {
            deleteNodeRecursive(child);
        }

        if (Objects.equals(node.getNodeType(), WorkspaceNodeType.MARKDOWN.getCode())) {
            workspaceContentDao.delete(new LambdaQueryWrapper<WorkspaceContent>()
                    .eq(WorkspaceContent::getNodeId, node.getId()));
        }
        workspaceNodeDao.deleteById(node.getId());
    }

    private List<WorkspaceNodeTreeDTO> buildTree(List<WorkspaceNode> nodes) {
        Map<Long, WorkspaceNodeTreeDTO> dtoMap = new HashMap<>();
        List<WorkspaceNodeTreeDTO> roots = new ArrayList<>();

        for (WorkspaceNode node : nodes) {
            dtoMap.put(node.getId(), WorkspaceNodeTreeDTO.builder()
                    .id(node.getId())
                    .parentId(node.getParentId())
                    .nodeType(node.getNodeType())
                    .title(node.getTitle())
                    .sortOrder(node.getSortOrder())
                    .children(new ArrayList<>())
                    .build());
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
                .eq(WorkspaceNode::getDelFlag, 0)
                .orderByDesc(WorkspaceNode::getSortOrder)
                .last("LIMIT 1");
        wrapper.eq(WorkspaceNode::getParentId, parentId);

        WorkspaceNode last = workspaceNodeDao.selectOne(wrapper);
        return last == null ? 0 : last.getSortOrder() + 1;
    }

    private WorkspaceNode requireExistingNode(Long nodeId) {
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null || Objects.equals(node.getDelFlag(), 1)) {
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
