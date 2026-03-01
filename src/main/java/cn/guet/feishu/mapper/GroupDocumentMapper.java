package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.GroupDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GroupDocumentMapper {
    
    void insert(GroupDocument document);
    
    void update(GroupDocument document);
    
    void deleteById(@Param("documentId") String documentId);
    
    GroupDocument findById(@Param("documentId") String documentId);
    
    List<GroupDocument> findByGroupId(@Param("groupId") String groupId);
    
    List<GroupDocument> findByFolderId(@Param("folderId") String folderId);
    
    int countByFolderId(@Param("folderId") String folderId);
    
    void incrementDownloadCount(@Param("documentId") String documentId);
}

