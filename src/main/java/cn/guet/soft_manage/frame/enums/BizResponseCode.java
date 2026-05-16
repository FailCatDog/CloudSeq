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

    UNAUTHORIZED(20000, "未登录或登录已过期"),
    LOGIN_FAILED(20001, "账号或密码错误"),
    TOKEN_INVALID(20002, "Token无效"),

    FORBIDDEN(30000, "无权限访问"),

    USER_NOT_FOUND(40000, "用户不存在");

    private final int code;
    private final String message;

}
