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
    USER_ROLE_STUDENT("STUDENT", "学生"),
    USER_ROLE_TEACHER("TEACHER", "教师"),
    USER_ROLE_ADMIN("ADMIN", "管理员"),

    DATA_SCOPE("data_scope", "RBAC数据范围"),
    DATA_SCOPE_SELF("SELF", "仅本人"),
    DATA_SCOPE_COURSE("COURSE", "关联课号"),
    DATA_SCOPE_ALL("ALL", "全部课号"),

    SYS_STATUS("sys_status", "RBAC启用状态"),
    SYS_STATUS_ACTIVE("ACTIVE", "启用"),
    SYS_STATUS_DISABLED("DISABLED", "禁用"),

    MENU_TYPE("menu_type", "菜单类型"),
    MENU_TYPE_DIR("M", "目录"),
    MENU_TYPE_MENU("C", "菜单"),
    MENU_TYPE_BUTTON("F", "按钮"),

    ROUTE_MATCH("route_match", "路由匹配"),
    ROUTE_MATCH_EXACT("EXACT", "完整路径"),
    ROUTE_MATCH_PREFIX("PREFIX", "前缀匹配"),

    MENU_VISIBLE("menu_visible", "菜单可见性"),
    MENU_VISIBLE_SHOW("SHOW", "显示"),
    MENU_VISIBLE_HIDE("HIDE", "隐藏"),

    HTTP_METHOD("http_method", "HTTP方法"),
    HTTP_METHOD_GET("GET", "GET"),
    HTTP_METHOD_POST("POST", "POST"),
    HTTP_METHOD_PUT("PUT", "PUT"),
    HTTP_METHOD_DELETE("DELETE", "DELETE"),
    HTTP_METHOD_PATCH("PATCH", "PATCH"),

    WORKSPACE_NODE_TYPE("workspace_node_type", "工作区节点类型"),
    WORKSPACE_NODE_TYPE_FOLDER("FOLDER", "文件夹"),
    WORKSPACE_NODE_TYPE_DOCUMENT("DOCUMENT", "文档"),
    WORKSPACE_NODE_TYPE_SHEET("SHEET", "表格"),

    COURSE_STATUS("course_status", "课号状态"),
    COURSE_STATUS_DRAFT("DRAFT", "草稿"),
    COURSE_STATUS_ACTIVE("ACTIVE", "进行中"),
    COURSE_STATUS_ARCHIVED("ARCHIVED", "已归档"),

    TERM_SEASON("term_season", "学期"),
    TERM_SEASON_SPRING("SPRING", "春季"),
    TERM_SEASON_SUMMER("SUMMER", "夏季"),
    TERM_SEASON_AUTUMN("AUTUMN", "秋季"),
    TERM_SEASON_WINTER("WINTER", "冬季"),

    COURSE_STAFF_ROLE("course_staff_role", "课号教务角色"),
    COURSE_STAFF_ROLE_TEACHER("TEACHER", "主讲"),
    COURSE_STAFF_ROLE_TA("TA", "助教"),
    COURSE_STAFF_ROLE_OBSERVER("OBSERVER", "督导"),

    COURSE_STAFF_STATUS("course_staff_status", "课号教务状态"),
    COURSE_STAFF_STATUS_ACTIVE("ACTIVE", "有效"),
    COURSE_STAFF_STATUS_INACTIVE("INACTIVE", "无效"),

    ENROLL_STATUS("enroll_status", "选课状态"),
    ENROLL_STATUS_ENROLLED("ENROLLED", "已选"),
    ENROLL_STATUS_DROPPED("DROPPED", "已退"),

    STUDENT_MILESTONE_STATUS("student_milestone_status", "学生里程碑状态"),
    STUDENT_MILESTONE_NEED_ENROLL("NEED_ENROLL", "待加入课号"),
    STUDENT_MILESTONE_NEED_TEAM("NEED_TEAM", "待加入小组"),
    STUDENT_MILESTONE_NEED_TOPIC("NEED_TOPIC", "待提交选题"),
    STUDENT_MILESTONE_TOPIC_PENDING("TOPIC_PENDING", "选题审批中"),
    STUDENT_MILESTONE_TOPIC_REJECTED("TOPIC_REJECTED", "选题被驳回"),
    STUDENT_MILESTONE_READY("READY", "可进入项目空间"),
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
