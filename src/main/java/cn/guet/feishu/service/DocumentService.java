package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.DocumentFolder;
import cn.guet.feishu.entity.GroupDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    
    DocumentFolder createFolder(String userId, CreateFolderRequestDTO dto);
    
    void deleteFolder(String userId, String folderId);
    
    void updateFolder(String userId, String folderId, UpdateFolderRequestDTO dto);
    
    List<DocumentFolder> getGroupFolders(String groupId);
    
    FolderContentDTO getFolderContent(String folderId);
    
    GroupDocument uploadDocument(String userId, String groupId, String folderId, 
                                 String description, MultipartFile file);
    
    void deleteDocument(String userId, String documentId);
    
    void updateDocument(String userId, String documentId, UpdateDocumentRequestDTO dto);
    
    GroupDocument getDocumentDetail(String documentId);
    
    List<GroupDocument> getGroupDocuments(String groupId);
    
    String downloadDocument(String userId, String documentId);
    
    void assignDocument(String userId, String documentId, List<AssignDocumentRequestDTO> assignments);
    
    void removeAssignment(String userId, String documentId, String targetUserId);
    
    void updateAssignment(String userId, String documentId, String targetUserId, String permission);
    
    List<DocumentAssignmentDTO> getDocumentAssignments(String documentId);
    
    List<GroupDocument> getMyDocuments(String userId);
}

