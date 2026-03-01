package cn.guet.feishu.service.impl;

import cn.guet.feishu.common.exception.BusinessException;
import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.*;
import cn.guet.feishu.mapper.*;
import cn.guet.feishu.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentFolderMapper documentFolderMapper;
    private final GroupDocumentMapper groupDocumentMapper;
    private final DocumentAssignmentMapper documentAssignmentMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    @Transactional
    public DocumentFolder createFolder(String userId, CreateFolderRequestDTO dto) {
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(dto.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以创建文件夹");
        }

        if (dto.getParentFolderId() != null) {
            DocumentFolder parentFolder = documentFolderMapper.findById(dto.getParentFolderId());
            if (parentFolder == null) {
                throw new BusinessException("父文件夹不存在");
            }
        }

        DocumentFolder folder = new DocumentFolder();
        folder.setFolderId(UUID.randomUUID().toString());
        folder.setGroupId(dto.getGroupId());
        folder.setFolderName(dto.getFolderName());
        folder.setParentFolderId(dto.getParentFolderId());
        folder.setCreatorId(userId);
        folder.setStatus(1);

        documentFolderMapper.insert(folder);
        return folder;
    }

    @Override
    @Transactional
    public void deleteFolder(String userId, String folderId) {
        DocumentFolder folder = documentFolderMapper.findById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(folder.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以删除文件夹");
        }

        deleteSubFoldersAndDocuments(folderId);
        documentFolderMapper.deleteById(folderId);
    }

    private void deleteSubFoldersAndDocuments(String folderId) {
        List<DocumentFolder> subFolders = documentFolderMapper.findByParentFolderId(folderId);
        for (DocumentFolder subFolder : subFolders) {
            deleteSubFoldersAndDocuments(subFolder.getFolderId());
            documentFolderMapper.deleteById(subFolder.getFolderId());
        }

        List<GroupDocument> documents = groupDocumentMapper.findByFolderId(folderId);
        for (GroupDocument document : documents) {
            deleteDocumentFile(document.getFilePath());
            documentAssignmentMapper.deleteByDocumentId(document.getDocumentId());
            groupDocumentMapper.deleteById(document.getDocumentId());
        }
    }

    @Override
    @Transactional
    public void updateFolder(String userId, String folderId, UpdateFolderRequestDTO dto) {
        DocumentFolder folder = documentFolderMapper.findById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(folder.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以更新文件夹");
        }

        folder.setFolderName(dto.getFolderName());
        documentFolderMapper.update(folder);
    }

    @Override
    public List<DocumentFolder> getGroupFolders(String groupId) {
        return documentFolderMapper.findByGroupId(groupId);
    }

    @Override
    public FolderContentDTO getFolderContent(String folderId) {
        DocumentFolder folder = documentFolderMapper.findById(folderId);
        if (folder == null) {
            throw new BusinessException("文件夹不存在");
        }

        FolderContentDTO content = new FolderContentDTO();
        content.setFolders(documentFolderMapper.findByParentFolderId(folderId));
        content.setDocuments(groupDocumentMapper.findByFolderId(folderId));
        return content;
    }

    @Override
    @Transactional
    public GroupDocument uploadDocument(String userId, String groupId, String folderId, String description, MultipartFile file) {
        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以上传文档");
        }

        if (folderId != null) {
            DocumentFolder folder = documentFolderMapper.findById(folderId);
            if (folder == null) {
                throw new BusinessException("文件夹不存在");
            }
        }

        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);
        String savedPath = saveFile(file);

        GroupDocument document = new GroupDocument();
        document.setDocumentId(UUID.randomUUID().toString());
        document.setGroupId(groupId);
        document.setFolderId(folderId);
        document.setDocumentName(originalFilename);
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setFilePath(savedPath);
        document.setDescription(description);
        document.setCreatorId(userId);
        document.setDownloadCount(0);
        document.setStatus(1);

        groupDocumentMapper.insert(document);
        return document;
    }

    private String saveFile(MultipartFile file) {
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadPath + File.separator + datePath;

        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("文件保存失败，请联系管理员");
        }

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = dirPath + File.separator + filename;

        try {
            file.transferTo(new File(filePath));
        } catch (IOException e) {
            throw new BusinessException("文件保存失败，请稍后重试");
        }

        return filePath;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    @Override
    @Transactional
    public void deleteDocument(String userId, String documentId) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(document.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以删除文档");
        }

        deleteDocumentFile(document.getFilePath());
        documentAssignmentMapper.deleteByDocumentId(documentId);
        groupDocumentMapper.deleteById(documentId);
    }

    private void deleteDocumentFile(String filePath) {
        if (filePath != null) {
            try {
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
            } catch (IOException e) {
            }
        }
    }

    @Override
    @Transactional
    public void updateDocument(String userId, String documentId, UpdateDocumentRequestDTO dto) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(document.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以更新文档");
        }

        if (dto.getDocumentName() != null) {
            document.setDocumentName(dto.getDocumentName());
        }
        if (dto.getDescription() != null) {
            document.setDescription(dto.getDescription());
        }
        if (dto.getFolderId() != null) {
            DocumentFolder folder = documentFolderMapper.findById(dto.getFolderId());
            if (folder == null) {
                throw new BusinessException("目标文件夹不存在");
            }
            document.setFolderId(dto.getFolderId());
        }

        groupDocumentMapper.update(document);
    }

    @Override
    public GroupDocument getDocumentDetail(String documentId) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        return document;
    }

    @Override
    public List<GroupDocument> getGroupDocuments(String groupId) {
        return groupDocumentMapper.findByGroupId(groupId);
    }

    @Override
    @Transactional
    public String downloadDocument(String userId, String documentId) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        groupDocumentMapper.incrementDownloadCount(documentId);
        return document.getFilePath();
    }

    @Override
    @Transactional
    public void assignDocument(String userId, String documentId, List<AssignDocumentRequestDTO> assignments) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(document.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以分配文档权限");
        }

        for (AssignDocumentRequestDTO dto : assignments) {
            DocumentAssignment existing = documentAssignmentMapper.findByDocumentIdAndUserId(documentId, dto.getUserId());
            if (existing != null) {
                continue;
            }

            DocumentAssignment assignment = new DocumentAssignment();
            assignment.setAssignmentId(UUID.randomUUID().toString());
            assignment.setDocumentId(documentId);
            assignment.setUserId(dto.getUserId());
            assignment.setAssignedBy(userId);
            assignment.setPermission(dto.getPermission());
            documentAssignmentMapper.insert(assignment);
        }
    }

    @Override
    @Transactional
    public void removeAssignment(String userId, String documentId, String targetUserId) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(document.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以移除文档权限");
        }

        documentAssignmentMapper.deleteByDocumentIdAndUserId(documentId, targetUserId);
    }

    @Override
    @Transactional
    public void updateAssignment(String userId, String documentId, String targetUserId, String permission) {
        GroupDocument document = groupDocumentMapper.findById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }

        GroupMember member = groupMemberMapper.selectByGroupIdAndUserId(document.getGroupId(), userId);
        if (member == null || !"LEADER".equals(member.getRoleInGroup())) {
            throw new BusinessException(403, "只有组长可以更新文档权限");
        }

        DocumentAssignment assignment = documentAssignmentMapper.findByDocumentIdAndUserId(documentId, targetUserId);
        if (assignment == null) {
            throw new BusinessException("权限分配不存在");
        }

        assignment.setPermission(permission);
        documentAssignmentMapper.update(assignment);
    }

    @Override
    public List<DocumentAssignmentDTO> getDocumentAssignments(String documentId) {
        List<DocumentAssignment> assignments = documentAssignmentMapper.findByDocumentId(documentId);
        return assignments.stream()
                .map(assignment -> {
                    User user = userMapper.selectByUserId(assignment.getUserId());
                    DocumentAssignmentDTO dto = new DocumentAssignmentDTO();
                    dto.setAssignmentId(assignment.getAssignmentId());
                    dto.setDocumentId(assignment.getDocumentId());
                    dto.setUserId(assignment.getUserId());
                    dto.setAssignedBy(assignment.getAssignedBy());
                    dto.setPermission(assignment.getPermission());
                    dto.setAssignTime(assignment.getAssignTime());
                    if (user != null) {
                        dto.setUsername(user.getUsername());
                        dto.setRealName(user.getRealName());
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    public List<GroupDocument> getMyDocuments(String userId) {
        List<DocumentAssignment> assignments = documentAssignmentMapper.findByUserId(userId);
        return assignments.stream()
                .map(assignment -> groupDocumentMapper.findById(assignment.getDocumentId()))
                .filter(doc -> doc != null)
                .toList();
    }
}

