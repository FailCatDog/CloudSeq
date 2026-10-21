package cn.guet.soft_manage.biz.rbac.controller;

import cn.guet.soft_manage.biz.rbac.dto.request.UserRoleAssignRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.AdminUserSummaryDTO;
import cn.guet.soft_manage.biz.rbac.service.IAdminUserRoleService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserRoleController {

    @Resource
    private IAdminUserRoleService adminUserRoleService;

    @GetMapping
    public Response<List<AdminUserSummaryDTO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleKey) {
        return Response.success(adminUserRoleService.search(keyword, roleKey));
    }

    @GetMapping("/{userId}/roles")
    public Response<AdminUserSummaryDTO> getUserRoles(@PathVariable Long userId) {
        return Response.success(adminUserRoleService.getUserRoles(userId));
    }

    @PutMapping("/{userId}/roles")
    public Response<Void> replaceUserRoles(@PathVariable Long userId,
            @Valid @RequestBody UserRoleAssignRequestDTO request) {
        adminUserRoleService.replaceUserRoles(userId, request.getRoleIds());
        return Response.success();
    }

    @PatchMapping("/{userId}/status")
    public Response<Void> updateStatus(@PathVariable Long userId, @RequestParam Integer isActive) {
        adminUserRoleService.updateUserStatus(userId, isActive);
        return Response.success();
    }
}
