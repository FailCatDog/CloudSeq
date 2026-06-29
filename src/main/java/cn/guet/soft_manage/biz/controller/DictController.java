package cn.guet.soft_manage.biz.controller;

import cn.guet.soft_manage.biz.pojo.dto.DictValueDTO;
import cn.guet.soft_manage.biz.service.DictService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典控制器
 */
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Resource
    private DictService dictService;

    @GetMapping("/values/{keyCode}")
    public Response<List<DictValueDTO>> listByKeyCode(@PathVariable String keyCode) {
        return Response.success(dictService.listByKeyCode(keyCode));
    }

    @GetMapping("/values/batch")
    public Response<Map<String, List<DictValueDTO>>> batch(@RequestParam String keyCodes) {
        List<String> codes = Arrays.stream(keyCodes.split(","))
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .toList();
        return Response.success(dictService.listByKeyCodes(codes));
    }

    /** 从 DB 全量重建 Redis 字典缓存并刷新本地内存 */
    @PostMapping("/cache/reload")
    public Response<Void> reloadCache() {
        dictService.reloadCache();
        return Response.success();
    }
}
