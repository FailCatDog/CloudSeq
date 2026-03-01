package cn.guet.feishu.mapper;

import cn.guet.feishu.entity.DocumentEdit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DocumentEditMapper {
    
    DocumentEdit findByDocumentId(@Param("documentId") String documentId);
    
    void insert(DocumentEdit documentEdit);
    
    void update(DocumentEdit documentEdit);
    
    void releaseEditLock(@Param("documentId") String documentId);
    
    void updateContent(@Param("documentId") String documentId, @Param("content") String content);
    
    void updateEditingTime(@Param("documentId") String documentId);
}

