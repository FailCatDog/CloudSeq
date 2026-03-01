package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class CreateFolderRequestDTO {
    private String groupId;
    private String folderName;
    private String parentFolderId;
}

