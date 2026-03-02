package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class CreateFolderRequestDTO {
    /**
     * 小组ID
     */
    private String groupId;
    /**
     * 文件夹名称
     */
    private String folderName;
    /**
     * 父文件夹ID
     */
    private String parentFolderId;
}

