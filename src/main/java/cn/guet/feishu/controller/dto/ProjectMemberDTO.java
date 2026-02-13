package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMemberDTO {
    private String projectMemberId;
    private String projectId;
    private String userId;
    private String roleInProject;
    private LocalDateTime joinTime;
    private Integer status;
    
    private String username;
    private String realName;
    private String studentId;
}

