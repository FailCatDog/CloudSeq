package cn.guet.feishu.controller.dto;

import cn.guet.feishu.entity.DocumentFolder;
import cn.guet.feishu.entity.GroupDocument;
import lombok.Data;

import java.util.List;

@Data
public class FolderContentDTO {
    private List<DocumentFolder> folders;
    private List<GroupDocument> documents;
}

