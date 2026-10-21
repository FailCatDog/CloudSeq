package cn.guet.soft_manage.biz.sheet.controller;

import cn.guet.soft_manage.biz.document.dto.CollabTokenResponseDTO;
import cn.guet.soft_manage.biz.sheet.dto.SheetDetailDTO;
import cn.guet.soft_manage.biz.sheet.service.SheetService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作区表格控制器
 */
@RestController
@RequestMapping("/api/sheets")
public class SheetController {

    @Resource
    private SheetService sheetService;

    @GetMapping("/{nodeId}")
    public Response<SheetDetailDTO> getSheet(@PathVariable Long nodeId) {
        return Response.success(sheetService.getSheet(nodeId));
    }

    @PostMapping("/{nodeId}/collab-token")
    public Response<CollabTokenResponseDTO> issueCollabToken(@PathVariable Long nodeId) {
        return Response.success(sheetService.issueCollabToken(nodeId));
    }
}
