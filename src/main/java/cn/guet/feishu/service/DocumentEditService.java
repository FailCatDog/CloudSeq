package cn.guet.feishu.service;

import cn.guet.feishu.entity.DocumentEdit;

public interface DocumentEditService {
    
    DocumentEdit getDocumentEdit(String documentId);
    
    DocumentEdit startEdit(String userId, String documentId);
    
    void saveContent(String documentId, String content);
    
    void releaseEdit(String userId, String documentId);
    
    void heartbeat(String userId, String documentId);
}

