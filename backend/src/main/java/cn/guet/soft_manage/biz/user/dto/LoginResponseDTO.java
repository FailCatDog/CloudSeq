package cn.guet.soft_manage.biz.user.dto;

import cn.guet.soft_manage.biz.rbac.dto.MenuTreeDTO;
import cn.guet.soft_manage.biz.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-17
 * @Description: 登录响应（含 RBAC 授权上下文）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    User user;
    String authorization;
    Set<String> permissions;
    List<MenuTreeDTO> menus;
    String home;
    String dataScope;
}
