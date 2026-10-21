package cn.guet.soft_manage.biz.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 侧栏菜单树节点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeDTO {

    private Long id;

    private Long parentId;

    private String menuName;

    private String menuType;

    private String path;

    private String routeMatch;

    private String perms;

    private String icon;

    private Integer sort;

    @Builder.Default
    private List<MenuTreeDTO> children = new ArrayList<>();
}
