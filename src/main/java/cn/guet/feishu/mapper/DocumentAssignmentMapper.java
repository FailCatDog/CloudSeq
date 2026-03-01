package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.DocumentAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentAssignmentMapper {
    
    void insert(DocumentAssignment assignment);
    
    void update(DocumentAssignment assignment);
    
    void deleteByDocumentIdAndUserId(@Param("documentId") String documentId, @Param("userId") String userId);
    
    void deleteByDocumentId(@Param("documentId") String documentId);
    
    DocumentAssignment findByDocumentIdAndUserId(@Param("documentId") String documentId, @Param("userId") String userId);
    
    List<DocumentAssignment> findByDocumentId(@Param("documentId") String documentId);
    
    List<DocumentAssignment> findByUserId(@Param("userId") String userId);
}

