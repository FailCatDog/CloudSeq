package cn.guet.soft_manage.biz.service.impl;

import cn.guet.soft_manage.biz.dao.WorkspaceFileDao;
import cn.guet.soft_manage.biz.pojo.entity.WorkspaceFile;
import cn.guet.soft_manage.biz.service.ObjectStorageService;
import cn.guet.soft_manage.biz.service.WorkspaceFileService;
import cn.guet.soft_manage.frame.constant.OfficeFileConstants;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 工作区 Office 文件服务实现
 */
@Service
public class WorkspaceFileServiceImpl implements WorkspaceFileService {

    @Resource
    private WorkspaceFileDao workspaceFileDao;

    @Resource
    private ObjectStorageService objectStorageService;

    @Override
    public WorkspaceFile getByNodeId(Long nodeId) {
        if (nodeId == null) {
            return null;
        }
        return workspaceFileDao.selectOne(new LambdaQueryWrapper<WorkspaceFile>()
                .eq(WorkspaceFile::getNodeId, nodeId)
                .eq(WorkspaceFile::getDelFlag, 0));
    }

    @Override
    public WorkspaceFile requireByNodeId(Long nodeId) {
        WorkspaceFile file = getByNodeId(nodeId);
        if (file == null) {
            throw new BusinessException(BizResponseCode.OFFICE_FILE_NOT_FOUND);
        }
        return file;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkspaceFile createBlankDocx(Long workspaceId, Long nodeId, String title, Long userId) {
        if (workspaceId == null || nodeId == null || userId == null) {
            throw new BusinessException(BizResponseCode.PARAM_ERROR);
        }

        String fileName = resolveFileName(title);
        String storageKey = OfficeFileConstants.buildStorageKey(workspaceId, nodeId);

        try {
            ClassPathResource resource = new ClassPathResource(OfficeFileConstants.BLANK_DOCX_TEMPLATE);
            if (!resource.exists()) {
                throw new BusinessException(BizResponseCode.STORAGE_ERROR);
            }
            byte[] bytes = resource.getInputStream().readAllBytes();
            try (InputStream template = new ByteArrayInputStream(bytes)) {
                objectStorageService.putObject(storageKey, template, bytes.length, OfficeFileConstants.DOCX_MIME);
            }

            WorkspaceFile file = WorkspaceFile.builder()
                    .nodeId(nodeId)
                    .storageKey(storageKey)
                    .fileName(fileName)
                    .mimeType(OfficeFileConstants.DOCX_MIME)
                    .fileSize((long) bytes.length)
                    .createUser(userId)
                    .updateUser(userId)
                    .build();
            workspaceFileDao.insert(file);
            return file;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(BizResponseCode.STORAGE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkspaceFile replaceContent(Long nodeId, InputStream inputStream, long size, Long userId) {
        WorkspaceFile file = requireByNodeId(nodeId);
        objectStorageService.putObject(file.getStorageKey(), inputStream, size, OfficeFileConstants.DOCX_MIME);

        file.setFileSize(size);
        file.setUpdateUser(userId);
        workspaceFileDao.updateById(file);
        return file;
    }

    @Override
    public InputStream openStream(WorkspaceFile file) {
        if (file == null || !StringUtils.hasText(file.getStorageKey())) {
            throw new BusinessException(BizResponseCode.OFFICE_FILE_NOT_FOUND);
        }
        return objectStorageService.getObject(file.getStorageKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByNodeId(Long nodeId) {
        WorkspaceFile file = getByNodeId(nodeId);
        if (file == null) {
            return;
        }
        objectStorageService.deleteObject(file.getStorageKey());
        workspaceFileDao.deleteById(file.getId());
    }

    private String resolveFileName(String title) {
        String base = StringUtils.hasText(title) ? title.trim() : "未命名文档";
        if (!base.toLowerCase().endsWith(OfficeFileConstants.DOCX_EXTENSION)) {
            base += OfficeFileConstants.DOCX_EXTENSION;
        }
        return base;
    }
}
