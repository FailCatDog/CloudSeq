package cn.guet.feishu.service;

import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.DocumentFolder;
import cn.guet.feishu.entity.GroupDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    
    /**
     * 创建文件夹
     * 
     * @param userId 用户ID
     * @param dto 创建请求，包含小组ID、文件夹名称、父文件夹ID
     * @return 创建的文件夹
     */
    DocumentFolder createFolder(String userId, CreateFolderRequestDTO dto);
    
    /**
     * 删除文件夹
     * 
     * @param userId 用户ID
     * @param folderId 文件夹ID
     */
    void deleteFolder(String userId, String folderId);
    
    /**
     * 更新文件夹
     * 
     * @param userId 用户ID
     * @param folderId 文件夹ID
     * @param dto 更新请求，包含文件夹名称
     */
    void updateFolder(String userId, String folderId, UpdateFolderRequestDTO dto);
    
    /**
     * 获取小组文件夹列表
     * 
     * @param groupId 小组ID
     * @return 文件夹列表
     */
    List<DocumentFolder> getGroupFolders(String groupId);
    
    /**
     * 获取文件夹内容
     * 
     * @param folderId 文件夹ID
     * @return 文件夹内容，包含子文件夹和文档
     */
    FolderContentDTO getFolderContent(String folderId);
    
    /**
     * 上传文档
     * 
     * @param userId 用户ID
     * @param groupId 小组ID
     * @param folderId 文件夹ID（可选）
     * @param description 文档描述（可选）
     * @param file 上传的文件
     * @return 上传的文档
     */
    GroupDocument uploadDocument(String userId, String groupId, String folderId, 
                                 String description, MultipartFile file);
    
    /**
     * 删除文档
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     */
    void deleteDocument(String userId, String documentId);
    
    /**
     * 更新文档信息
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @param dto 更新请求，包含文档名称、描述、文件夹ID
     */
    void updateDocument(String userId, String documentId, UpdateDocumentRequestDTO dto);
    
    /**
     * 获取文档详情
     * 
     * @param documentId 文档ID
     * @return 文档详情
     */
    GroupDocument getDocumentDetail(String documentId);
    
    /**
     * 获取小组文档列表
     * 
     * @param groupId 小组ID
     * @return 文档列表
     */
    List<GroupDocument> getGroupDocuments(String groupId);
    
    /**
     * 下载文档
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @return 文件路径
     */
    String downloadDocument(String userId, String documentId);
    
    /**
     * 分配文档权限
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @param assignments 分配列表，包含用户ID和权限
     */
    void assignDocument(String userId, String documentId, List<AssignDocumentRequestDTO> assignments);
    
    /**
     * 移除文档权限
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @param targetUserId 目标用户ID
     */
    void removeAssignment(String userId, String documentId, String targetUserId);
    
    /**
     * 更新文档权限
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @param targetUserId 目标用户ID
     * @param permission 权限
     */
    void updateAssignment(String userId, String documentId, String targetUserId, String permission);
    
    /**
     * 获取文档权限分配列表
     * 
     * @param documentId 文档ID
     * @return 分配列表
     */
    List<DocumentAssignmentDTO> getDocumentAssignments(String documentId);
    
    /**
     * 获取我的文档列表
     * 
     * @param userId 用户ID
     * @return 文档列表
     */
    List<GroupDocument> getMyDocuments(String userId);
}

