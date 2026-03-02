package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectMemberDTO {
    /**
     * 项目成员ID
     */
    private String projectMemberId;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 在项目中的角色
     */
    private String roleInProject;
    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 用户名
     */
    private String username;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 学号
     */
    private String studentId;
}

