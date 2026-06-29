package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作区访问上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceAccessContext {

    private Long userId;

    private Long workspaceId;

    private Long teamId;

    /** 教师等只读角色 */
    private boolean readOnly;

    /** 是否允许写入（成员 + 空间已解锁） */
    private boolean canWrite;
}
