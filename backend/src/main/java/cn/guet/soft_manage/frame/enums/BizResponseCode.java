package cn.guet.soft_manage.frame.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 业务响应码
 *
 * 规则：
 * 0      - 成功
 * 1xxxx  - 通用与系统
 * 2xxxx  - 认证与登录
 * 3xxxx  - 权限与角色
 * 4xxxx  - 用户模块
 * 5xxxx  - 课程/项目/教学业务模块
 * 6xxxx  - 文件/附件
 * 7xxxx  - 审批/通知/流程
 */
@Getter
@AllArgsConstructor
public enum BizResponseCode {

    SUCCESS(0, "success"),

    SYSTEM_ERROR(10000, "系统异常，请稍后重试"),
    PARAM_ERROR(10001, "请求参数错误"),
    REQUEST_BODY_INVALID(10002, "请求体格式错误"),
    DICT_KEY_NOT_FOUND(10003, "字典类型不存在"),

    UNAUTHORIZED(20000, "未登录"),
    TOKEN_EXPIRED(20001, "登录已过期，请重新登录"),
    LOGIN_FAILED(20002, "账号或密码错误"),
    TOKEN_INVALID(20003, "Token无效"),

    FORBIDDEN(30000, "无权限访问"),
    TEACHER_READ_ONLY(30001, "教师账号仅可查看文档"),

    USER_NOT_FOUND(40000, "用户不存在"),
    USERNAME_EXISTS(40001, "用户名已存在"),
    STUDENT_NO_REQUIRED(40002, "学生注册时学号不能为空"),

    WORKSPACE_NOT_FOUND(50000, "工作区不存在"),
    WORKSPACE_LOCKED(50001, "选题审批通过后方可编辑"),
    NODE_NOT_FOUND(50002, "节点不存在"),
    NODE_TYPE_INVALID(50003, "节点类型不支持该操作"),
    ROOT_NODE_NOT_DELETABLE(50004, "根目录不可删除"),

    WEEKLY_REPORT_NOT_FOUND(50005, "周报不存在"),
    WEEKLY_REPORT_DUPLICATE(50006, "该周周报已存在"),
    WEEKLY_REPORT_ID_REQUIRED(50007, "周报ID不能为空"),
    WEEKLY_REPORT_SUBMITTED(50008, "已提交的周报不可修改"),
    WEEKLY_REPORT_PROGRESS_REQUIRED(50009, "请填写本周工作进展后再提交"),
    WEEKLY_REPORT_STALE(50010, "周报不存在或已被修改"),

    PLAN_TASK_START_AFTER_END(50011, "开始日期不能晚于结束日期"),
    PLAN_TASK_ACTUAL_START_AFTER_END(50012, "实际开始日期不能晚于实际结束日期"),
    PLAN_TASK_ID_REQUIRED(50013, "任务ID不能为空"),
    PLAN_TASK_STALE(50014, "任务不存在或已被修改"),
    PLAN_TASK_NOT_FOUND(50015, "任务不存在"),
    NODE_TITLE_REQUIRED(50016, "标题不能为空"),
    NODE_PARENT_MUST_BE_FOLDER(50017, "只能在文件夹下创建节点"),
    WORKSPACE_ID_REQUIRED(50018, "工作区ID不能为空"),
    NODE_TYPE_CREATE_UNSUPPORTED(50019, "暂不支持创建该类型节点"),
    TEAM_NOT_FOUND(50020, "小组不存在"),
    TEAM_LEADER_ALREADY_ASSIGNED(50021, "该用户已经是其他小组组长"),
    TEAM_MEMBER_ALREADY_EXISTS(50022, "该用户已经在小组中"),
    TEAM_MEMBER_NOT_FOUND(50023, "成员不存在"),
    TEAM_LEADER_CANNOT_QUIT(50024, "组长不能直接退出，请先转让组长"),
    NOT_IN_TEAM(50025, "您尚未加入小组"),
    DOCUMENT_CONTENT_NOT_FOUND(50026, "文档正文不存在"),
    DOCUMENT_STALE(50027, "文档已被他人修改，请刷新后重试"),
    DOCUMENT_VERSION_REQUIRED(50028, "版本号不能为空"),
    COLLAB_TOKEN_INVALID(50029, "协同令牌无效"),
    COLLAB_TOKEN_EXPIRED(50030, "协同令牌已过期"),
    DOCUMENT_COMMENT_NOT_FOUND(50031, "评论不存在"),
    DOCUMENT_COMMENT_CONTENT_REQUIRED(50032, "评论内容不能为空"),
    DOCUMENT_COMMENT_ANCHOR_REQUIRED(50033, "评论锚点位置无效"),

    OBJECT_STORAGE_ERROR(60000, "对象存储操作失败"),
    OBJECT_STORAGE_DISABLED(60001, "对象存储未启用"),
    ASSET_NOT_FOUND(60002, "资产不存在"),
    FILE_TOO_LARGE(60003, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(60004, "不支持的文件类型"),
    FILE_EMPTY(60005, "上传文件不能为空"),

    APPROVAL_NOT_FOUND(70000, "审批记录不存在");

    private final int code;
    private final String message;

}
