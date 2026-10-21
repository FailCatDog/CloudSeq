package cn.guet.soft_manage.biz.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 登录授权上下文：权限码 + 菜单树 + 首页 + 数据范围
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthContextDTO {

    private Set<String> permissions;

    private List<MenuTreeDTO> menus;

    private String home;

    private String dataScope;
}
