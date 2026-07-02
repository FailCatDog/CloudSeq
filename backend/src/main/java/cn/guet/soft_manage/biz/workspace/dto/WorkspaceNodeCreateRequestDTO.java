package cn.guet.soft_manage.biz.workspace.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 创建节点请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceNodeCreateRequestDTO {

    private Long workspaceId;

    private Long parentId;

    private String nodeType;

    private String title;
}
