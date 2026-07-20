package cn.guet.soft_manage.biz.rbac.controller;

import cn.guet.soft_manage.biz.rbac.dto.request.RoleMenuAssignRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.request.RoleSaveRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.RoleSummaryDTO;
import cn.guet.soft_manage.biz.rbac.service.IAdminRoleService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    @Resource
    private IAdminRoleService adminRoleService;

    @GetMapping
    public Response<List<RoleSummaryDTO>> list(@RequestParam(required = false) String status) {
        return Response.success(adminRoleService.list(status));
    }

    @GetMapping("/{id}")
    public Response<RoleSummaryDTO> getById(@PathVariable Long id) {
        return Response.success(adminRoleService.getById(id));
    }

    @PostMapping
    public Response<RoleSummaryDTO> create(@Valid @RequestBody RoleSaveRequestDTO request) {
        return Response.success(adminRoleService.create(request));
    }

    @PutMapping("/{id}")
    public Response<RoleSummaryDTO> update(@PathVariable Long id,
            @Valid @RequestBody RoleSaveRequestDTO request) {
        return Response.success(adminRoleService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Response<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminRoleService.updateStatus(id, status);
        return Response.success();
    }

    @GetMapping("/{id}/menu-ids")
    public Response<List<Long>> listMenuIds(@PathVariable Long id) {
        return Response.success(adminRoleService.listMenuIds(id));
    }

    @PutMapping("/{id}/menu-ids")
    public Response<Void> replaceMenuIds(@PathVariable Long id,
            @RequestBody RoleMenuAssignRequestDTO request) {
        adminRoleService.replaceMenuIds(id, request == null ? null : request.getMenuIds());
        return Response.success();
    }
}
