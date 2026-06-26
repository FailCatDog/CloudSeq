package cn.guet.soft_manage.biz.service;

import cn.guet.soft_manage.biz.pojo.entity.WorkspaceFile;

import java.io.InputStream;

/**
 * 工作区 Office 文件服务
 */
public interface WorkspaceFileService {

    WorkspaceFile getByNodeId(Long nodeId);

    WorkspaceFile requireByNodeId(Long nodeId);

    /** 为新建 Office 节点创建空白 docx 并写入 MinIO */
    WorkspaceFile createBlankDocx(Long workspaceId, Long nodeId, String title, Long userId);

    /** OnlyOffice 保存后替换文件内容并递增 version */
    WorkspaceFile replaceContent(Long nodeId, InputStream inputStream, long size, Long userId);

    InputStream openStream(WorkspaceFile file);

    void deleteByNodeId(Long nodeId);
}
