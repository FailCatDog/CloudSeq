package cn.guet.soft_manage.frame.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区节点类型
 */
@Getter
@AllArgsConstructor
public enum WorkspaceNodeType {

    FOLDER(1),
    DOCUMENT(2),
    ;

    private final int code;

    public static WorkspaceNodeType of(int code) {
        for (WorkspaceNodeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
