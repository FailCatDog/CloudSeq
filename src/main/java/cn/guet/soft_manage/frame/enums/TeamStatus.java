package cn.guet.soft_manage.frame.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 小组状态
 */
@Getter
@AllArgsConstructor
public enum TeamStatus {

    NORMAL(1),
    PENDING_TOPIC(2),
    TOPIC_REJECTED(3),
    UNLOCKED(4),
    ;

    private final int code;
}
