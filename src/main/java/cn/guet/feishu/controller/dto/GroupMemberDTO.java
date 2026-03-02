package cn.guet.feishu.controller.dto;

import lombok.Data;

@Data
public class GroupMemberDTO {
    /**
     * 小组成员ID
     */
    private String groupMemberId;
    /**
     * 小组ID
     */
    private String groupId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 在小组中的角色
     */
    private String roleInGroup;
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

