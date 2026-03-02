package cn.guet.feishu.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupRequestDTO {
    /**
     * 项目ID
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;
    /**
     * 小组名称
     */
    @NotBlank(message = "小组名称不能为空")
    private String groupName;
    /**
     * 小组描述
     */
    private String description;
}

