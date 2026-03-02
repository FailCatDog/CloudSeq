package cn.guet.feishu.controller.dto;

import cn.guet.feishu.entity.DocumentFolder;
import cn.guet.feishu.entity.GroupDocument;
import lombok.Data;

import java.util.List;

@Data
public class FolderContentDTO {
    /**
     * 子文件夹列表
     */
    private List<DocumentFolder> folders;
    /**
     * 文档列表
     */
    private List<GroupDocument> documents;
}

