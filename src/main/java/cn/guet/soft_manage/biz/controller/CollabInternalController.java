package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.CollabLoadResponseDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabPersistRequestDTO;
import cn.guet.soft_manage.biz.pojo.dto.CollabPersistResponseDTO;
import cn.guet.soft_manage.biz.service.DocumentService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 协同服务内部 API 控制器
 */
@RestController
@RequestMapping("/api/internal/collab")
public class CollabInternalController {

    @Resource
    private DocumentService documentService;

    @GetMapping("/load/{nodeId}")
    public Response<CollabLoadResponseDTO> load(@PathVariable Long nodeId) {
        return Response.success(documentService.loadForCollab(nodeId));
    }

    @PostMapping("/persist")
    public Response<CollabPersistResponseDTO> persist(@RequestBody CollabPersistRequestDTO request) {
        return Response.success(documentService.persistFromCollab(request));
    }
}
