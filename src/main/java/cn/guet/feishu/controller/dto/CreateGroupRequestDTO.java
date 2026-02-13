package cn.guet.feishu.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupRequestDTO {
    
    @NotBlank(message = "项目ID不能为空")
    private String projectId;
    
    @NotBlank(message = "小组名称不能为空")
    private String groupName;
    
    private String description;
}

