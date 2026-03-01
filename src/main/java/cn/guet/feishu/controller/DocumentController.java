package cn.guet.feishu.controller;

import cn.guet.feishu.common.result.Result;
import cn.guet.feishu.controller.dto.*;
import cn.guet.feishu.entity.DocumentFolder;
import cn.guet.feishu.entity.GroupDocument;
import cn.guet.feishu.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/folder")
    public Result<DocumentFolder> createFolder(HttpServletRequest request,
                                                @Valid @RequestBody CreateFolderRequestDTO dto) {
        String userId = (String) request.getAttribute("userId");
        DocumentFolder folder = documentService.createFolder(userId, dto);
        return Result.success(folder);
    }

    @DeleteMapping("/folder/{folderId}")
    public Result<Void> deleteFolder(HttpServletRequest request,
                                      @PathVariable String folderId) {
        String userId = (String) request.getAttribute("userId");
        documentService.deleteFolder(userId, folderId);
        return Result.success();
    }

    @PutMapping("/folder/{folderId}")
    public Result<Void> updateFolder(HttpServletRequest request,
                                      @PathVariable String folderId,
                                      @RequestParam String folderName) {
        String userId = (String) request.getAttribute("userId");
        UpdateFolderRequestDTO dto = new UpdateFolderRequestDTO();
        dto.setFolderName(folderName);
        documentService.updateFolder(userId, folderId, dto);
        return Result.success();
    }

    @GetMapping("/folder/group/{groupId}")
    public Result<List<DocumentFolder>> getGroupFolders(@PathVariable String groupId) {
        List<DocumentFolder> folders = documentService.getGroupFolders(groupId);
        return Result.success(folders);
    }

    @GetMapping("/folder/{folderId}/content")
    public Result<FolderContentDTO> getFolderContent(@PathVariable String folderId) {
        FolderContentDTO content = documentService.getFolderContent(folderId);
        return Result.success(content);
    }

    @PostMapping("/upload")
    public Result<GroupDocument> uploadDocument(HttpServletRequest request,
                                                 @RequestParam String groupId,
                                                 @RequestParam(required = false) String folderId,
                                                 @RequestParam(required = false) String description,
                                                 @RequestParam("file") MultipartFile file) {
        String userId = (String) request.getAttribute("userId");
        log.info("用户上传文档: userId={} fileName={}", userId, file.getOriginalFilename());
        GroupDocument document = documentService.uploadDocument(userId, groupId, folderId, description, file);
        return Result.success(document);
    }

    @DeleteMapping("/{documentId}")
    public Result<Void> deleteDocument(HttpServletRequest request,
                                        @PathVariable String documentId) {
        String userId = (String) request.getAttribute("userId");
        documentService.deleteDocument(userId, documentId);
        return Result.success();
    }

    @PutMapping("/{documentId}")
    public Result<Void> updateDocument(HttpServletRequest request,
                                        @PathVariable String documentId,
                                        @Valid @RequestBody UpdateDocumentRequestDTO dto) {
        String userId = (String) request.getAttribute("userId");
        documentService.updateDocument(userId, documentId, dto);
        return Result.success();
    }

    @GetMapping("/{documentId}")
    public Result<GroupDocument> getDocumentDetail(@PathVariable String documentId) {
        GroupDocument document = documentService.getDocumentDetail(documentId);
        return Result.success(document);
    }

    @GetMapping("/group/{groupId}")
    public Result<List<GroupDocument>> getGroupDocuments(@PathVariable String groupId) {
        List<GroupDocument> documents = documentService.getGroupDocuments(groupId);
        return Result.success(documents);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(HttpServletRequest request,
                                                      @PathVariable String documentId,
                                                      HttpServletResponse response) throws MalformedURLException {
        String userId = (String) request.getAttribute("userId");
        String filePath = documentService.downloadDocument(userId, documentId);
        
        Path path = Paths.get(filePath);
        Resource resource = new UrlResource(path.toUri());
        
        String filename = path.getFileName().toString();
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/{documentId}/assign")
    public Result<Void> assignDocument(HttpServletRequest request,
                                        @PathVariable String documentId,
                                        @Valid @RequestBody List<AssignDocumentRequestDTO> assignments) {
        String userId = (String) request.getAttribute("userId");
        documentService.assignDocument(userId, documentId, assignments);
        return Result.success();
    }

    @DeleteMapping("/{documentId}/assign/{userId}")
    public Result<Void> removeAssignment(HttpServletRequest request,
                                          @PathVariable String documentId,
                                          @PathVariable String userId) {
        String currentUserId = (String) request.getAttribute("userId");
        documentService.removeAssignment(currentUserId, documentId, userId);
        return Result.success();
    }

    @PutMapping("/{documentId}/assign/{userId}")
    public Result<Void> updateAssignment(HttpServletRequest request,
                                          @PathVariable String documentId,
                                          @PathVariable String userId,
                                          @RequestParam String permission) {
        String currentUserId = (String) request.getAttribute("userId");
        documentService.updateAssignment(currentUserId, documentId, userId, permission);
        return Result.success();
    }

    @GetMapping("/{documentId}/assignments")
    public Result<List<DocumentAssignmentDTO>> getDocumentAssignments(@PathVariable String documentId) {
        List<DocumentAssignmentDTO> assignments = documentService.getDocumentAssignments(documentId);
        return Result.success(assignments);
    }

    @GetMapping("/my")
    public Result<List<GroupDocument>> getMyDocuments(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<GroupDocument> documents = documentService.getMyDocuments(userId);
        return Result.success(documents);
    }
}

