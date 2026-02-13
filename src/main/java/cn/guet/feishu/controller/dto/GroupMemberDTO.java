package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class GroupMemberDTO {
    private String groupMemberId;
    private String groupId;
    private String userId;
    private String roleInGroup;
    
    private String username;
    private String realName;
    private String studentId;
}

