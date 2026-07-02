package cn.guet.soft_manage.biz.workspace.controller;

import cn.guet.soft_manage.biz.workspace.entity.Workspace;
import cn.guet.soft_manage.biz.workspace.service.WorkspaceService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 工作区控制器
 */
@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {

    @Resource
    private WorkspaceService workspaceService;

    @GetMapping("/current")
    public Response<Workspace> getCurrent() {
        return Response.success(workspaceService.getCurrentTeamWorkspace());
    }
}
