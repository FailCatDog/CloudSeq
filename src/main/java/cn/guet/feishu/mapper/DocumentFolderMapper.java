package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.DocumentFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentFolderMapper {
    
    void insert(DocumentFolder folder);
    
    void update(DocumentFolder folder);
    
    void deleteById(@Param("folderId") String folderId);
    
    DocumentFolder findById(@Param("folderId") String folderId);
    
    List<DocumentFolder> findByGroupId(@Param("groupId") String groupId);
    
    List<DocumentFolder> findByParentFolderId(@Param("parentFolderId") String parentFolderId);
    
    int countByParentFolderId(@Param("parentFolderId") String parentFolderId);
}

