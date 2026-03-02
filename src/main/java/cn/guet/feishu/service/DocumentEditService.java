package cn.guet.feishu.service;

import cn.guet.feishu.entity.DocumentEdit;

public interface DocumentEditService {
    
    /**
     * 获取文档编辑内容
     * 
     * @param documentId 文档ID
     * @return 文档编辑信息
     */
    DocumentEdit getDocumentEdit(String documentId);
    
    /**
     * 开始编辑文档
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     * @return 文档编辑信息
     */
    DocumentEdit startEdit(String userId, String documentId);
    
    /**
     * 保存文档内容
     * 
     * @param documentId 文档ID
     * @param content 文档内容
     */
    void saveContent(String documentId, String content);
    
    /**
     * 释放编辑锁
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     */
    void releaseEdit(String userId, String documentId);
    
    /**
     * 编辑心跳
     * 
     * @param userId 用户ID
     * @param documentId 文档ID
     */
    void heartbeat(String userId, String documentId);
}

