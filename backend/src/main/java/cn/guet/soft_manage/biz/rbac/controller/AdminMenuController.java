package cn.guet.soft_manage.biz.rbac.controller;

import cn.guet.soft_manage.biz.rbac.dto.request.MenuSaveRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.AdminMenuNodeDTO;
import cn.guet.soft_manage.biz.rbac.service.IAdminMenuService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/menus")
public class AdminMenuController {

    @Resource
    private IAdminMenuService adminMenuService;

    @GetMapping("/tree")
    public Response<List<AdminMenuNodeDTO>> tree() {
        return Response.success(adminMenuService.listTree());
    }

    @GetMapping("/{id}")
    public Response<AdminMenuNodeDTO> getById(@PathVariable Long id) {
        return Response.success(adminMenuService.getById(id));
    }

    @PostMapping
    public Response<AdminMenuNodeDTO> create(@Valid @RequestBody MenuSaveRequestDTO request) {
        return Response.success(adminMenuService.create(request));
    }

    @PutMapping("/{id}")
    public Response<AdminMenuNodeDTO> update(@PathVariable Long id,
            @Valid @RequestBody MenuSaveRequestDTO request) {
        return Response.success(adminMenuService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Response<Void> delete(@PathVariable Long id) {
        adminMenuService.delete(id);
        return Response.success();
    }

    @PatchMapping("/{id}/status")
    public Response<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminMenuService.updateStatus(id, status);
        return Response.success();
    }
}
