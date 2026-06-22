package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区目录树节点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceNodeTreeDTO {

    private Long id;

    private Long parentId;

    private Integer nodeType;

    private String title;

    private Integer sortOrder;

    private List<WorkspaceNodeTreeDTO> children;
}
