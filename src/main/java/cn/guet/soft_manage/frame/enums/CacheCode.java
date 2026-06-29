package cn.guet.soft_manage.frame.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典 Key / Value 编码枚举
 */
@Getter
@AllArgsConstructor
public enum CacheCode {

    TEAM_STATUS("TEAM_STATUS", "小组状态"),
    TEAM_STATUS_NORMAL("NORMAL", "正常"),
    TEAM_STATUS_PENDING_TOPIC("PENDING_TOPIC", "待选题审批"),
    TEAM_STATUS_TOPIC_REJECTED("TOPIC_REJECTED", "选题驳回"),
    TEAM_STATUS_UNLOCKED("UNLOCKED", "已解锁"),

    MEMBER_STATUS("member_status", "成员状态"),
    MEMBER_STATUS_ACTIVE("ACTIVE", "在组"),
    MEMBER_STATUS_LEFT("LEFT", "离组"),

    APPROVAL_STATUS("approval_status", "选题审批状态"),
    APPROVAL_STATUS_PENDING("PENDING", "待审"),
    APPROVAL_STATUS_APPROVED("APPROVED", "通过"),
    APPROVAL_STATUS_REJECTED("REJECTED", "驳回"),

    PLAN_TASK_STATUS("plan_task_status", "计划任务状态"),
    PLAN_TASK_STATUS_NOT_STARTED("NOT_STARTED", "未开始"),
    PLAN_TASK_STATUS_IN_PROGRESS("IN_PROGRESS", "进行中"),
    PLAN_TASK_STATUS_COMPLETED("COMPLETED", "已完成"),
    PLAN_TASK_STATUS_CANCELLED("CANCELLED", "已取消"),

    WEEKLY_REPORT_STATUS("weekly_report_status", "周报状态"),
    WEEKLY_REPORT_STATUS_DRAFT("DRAFT", "草稿"),
    WEEKLY_REPORT_STATUS_SUBMITTED("SUBMITTED", "已提交"),

    USER_ROLE("user_role", "用户角色"),
    USER_ROLE_TEACHER("TEACHER", "教师"),
    USER_ROLE_STUDENT("STUDENT", "学生"),

    WORKSPACE_NODE_TYPE("workspace_node_type", "工作区节点类型"),
    WORKSPACE_NODE_TYPE_FOLDER("FOLDER", "文件夹"),
    WORKSPACE_NODE_TYPE_DOCUMENT("DOCUMENT", "文档"),
    ;

    private final String code;
    private final String name;

    public boolean isDictKey() {
        return code.equals(code.toLowerCase());
    }

    public static CacheCode of(String code) {
        if (code == null) return null;
        for (CacheCode item : values()) {
            if (item.code.equals(code)) return item;
        }
        return null;
    }
}
