package cn.guet.soft_manage.biz.document.service.impl;

import cn.guet.soft_manage.biz.document.dao.WorkspaceAssetDao;
import cn.guet.soft_manage.biz.workspace.dao.WorkspaceNodeDao;
import cn.guet.soft_manage.biz.document.dto.DocumentAssetReadDTO;
import cn.guet.soft_manage.biz.document.dto.DocumentAssetUploadResponseDTO;
import cn.guet.soft_manage.biz.document.entity.WorkspaceAsset;
import cn.guet.soft_manage.biz.workspace.entity.WorkspaceNode;
import cn.guet.soft_manage.biz.document.service.DocumentAssetService;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceAccessService;
import cn.guet.soft_manage.biz.document.util.DocumentAssetUrlHelper;
import cn.guet.soft_manage.biz.document.util.DocumentAssetValidator;
import cn.guet.soft_manage.frame.auth.UserContext;
import cn.guet.soft_manage.frame.config.DocumentAssetProperties;
import cn.guet.soft_manage.frame.constant.MinioConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.enums.CacheCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import cn.guet.soft_manage.frame.storage.ObjectStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnBean(ObjectStorageService.class)
public class DocumentAssetServiceImpl implements DocumentAssetService {

    @Resource
    private WorkspaceNodeDao workspaceNodeDao;

    @Resource
    private WorkspaceAssetDao workspaceAssetDao;

    @Resource
    private WorkspaceAccessService workspaceAccessService;

    @Resource
    private ObjectStorageService objectStorageService;

    @Resource
    private DocumentAssetProperties documentAssetProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocumentAssetUploadResponseDTO uploadAsset(Long nodeId, MultipartFile file) {
        if (nodeId == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }

        WorkspaceNode node = requireDocumentNode(nodeId);
        workspaceAccessService.requireWritableWorkspace(node.getWorkspaceId());
        DocumentAssetValidator.validateUpload(file, documentAssetProperties);

        String contentType = DocumentAssetValidator.normalizeContentType(file.getContentType());
        String extension = DocumentAssetValidator.extractExtension(file.getOriginalFilename());
        String storageKey = buildStorageKey(node.getWorkspaceId(), nodeId, extension);
        String assetType = DocumentAssetValidator.resolveAssetType(contentType);
        String originalName = sanitizeOriginalName(file.getOriginalFilename());

        final byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException(BizResponseCode.OBJECT_STORAGE_ERROR);
        }
        if (fileBytes.length == 0) {
            throw new BusinessException(BizResponseCode.FILE_EMPTY);
        }

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            objectStorageService.putObject(
                    MinioConstants.BUCKET_WORKSPACE_ASSETS,
                    storageKey,
                    inputStream,
                    fileBytes.length,
                    contentType
            );
        } catch (IOException ex) {
            throw new BusinessException(BizResponseCode.OBJECT_STORAGE_ERROR);
        }

        Long userId = UserContext.getUserId();
        LocalDateTime now = LocalDateTime.now();
        WorkspaceAsset asset = WorkspaceAsset.builder()
                .workspaceId(node.getWorkspaceId())
                .nodeId(nodeId)
                .storageKey(storageKey)
                .originalName(originalName)
                .contentType(contentType)
                .sizeBytes((long) fileBytes.length)
                .assetType(assetType)
                .createUser(userId)
                .createDate(now)
                .updateUser(userId)
                .updateDate(now)
                .build();
        workspaceAssetDao.insert(asset);

        return DocumentAssetUploadResponseDTO.builder()
                .assetId(asset.getId())
                .nodeId(nodeId)
                .workspaceId(node.getWorkspaceId())
                .url(DocumentAssetUrlHelper.toProxyUrl(asset.getId()))
                .fileName(originalName)
                .contentType(contentType)
                .size((long) fileBytes.length)
                .assetType(assetType)
                .build();
    }

    @Override
    public DocumentAssetReadDTO readAsset(Long assetId) {
        if (assetId == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }

        WorkspaceAsset asset = workspaceAssetDao.selectById(assetId);
        if (asset == null) {
            throw new BusinessException(BizResponseCode.ASSET_NOT_FOUND);
        }

        workspaceAccessService.requireCurrentAccess(asset.getWorkspaceId());

        InputStream inputStream = objectStorageService.getObject(
                MinioConstants.BUCKET_WORKSPACE_ASSETS,
                asset.getStorageKey()
        );

        return DocumentAssetReadDTO.builder()
                .inputStream(inputStream)
                .contentType(asset.getContentType())
                .fileName(asset.getOriginalName())
                .sizeBytes(asset.getSizeBytes())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAssetsByNodeId(Long nodeId) {
        if (nodeId == null) {
            return;
        }

        List<WorkspaceAsset> assets = workspaceAssetDao.selectList(new LambdaQueryWrapper<WorkspaceAsset>()
                .eq(WorkspaceAsset::getNodeId, nodeId));
        if (assets.isEmpty()) {
            return;
        }

        for (WorkspaceAsset asset : assets) {
            try {
                objectStorageService.removeObject(
                        MinioConstants.BUCKET_WORKSPACE_ASSETS,
                        asset.getStorageKey()
                );
            } catch (Exception ex) {
                log.warn("Failed to remove asset object from storage, assetId={}, key={}",
                        asset.getId(), asset.getStorageKey(), ex);
            }
            workspaceAssetDao.deleteById(asset.getId());
        }
    }

    private WorkspaceNode requireDocumentNode(Long nodeId) {
        WorkspaceNode node = workspaceNodeDao.selectById(nodeId);
        if (node == null) {
            throw new BusinessException(BizResponseCode.NODE_NOT_FOUND);
        }
        if (!Objects.equals(node.getNodeType(), CacheCode.WORKSPACE_NODE_TYPE_DOCUMENT.getCode())) {
            throw new BusinessException(BizResponseCode.NODE_TYPE_INVALID);
        }
        return node;
    }

    private static String buildStorageKey(Long workspaceId, Long nodeId, String extension) {
        return MinioConstants.PATH_WORKSPACE_ASSETS
                + "/" + workspaceId
                + "/" + nodeId
                + "/" + UUID.randomUUID()
                + extension;
    }

    private static String sanitizeOriginalName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "unnamed";
        }
        String name = originalFilename.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        if (name.length() > 200) {
            name = name.substring(0, 200);
        }
        return name;
    }
}
