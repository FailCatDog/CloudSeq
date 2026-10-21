package cn.guet.soft_manage.biz.dict.controller;

import cn.guet.soft_manage.biz.dict.dto.request.DictKeySaveRequestDTO;
import cn.guet.soft_manage.biz.dict.dto.request.DictValueSaveRequestDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictKeyDetailDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictKeySummaryDTO;
import cn.guet.soft_manage.biz.dict.dto.response.DictValueAdminDTO;
import cn.guet.soft_manage.biz.dict.service.IAdminDictService;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dicts")
public class AdminDictController {

    @Resource
    private IAdminDictService adminDictService;

    @GetMapping
    public Response<List<DictKeySummaryDTO>> listKeys() {
        return Response.success(adminDictService.listKeys());
    }

    @GetMapping("/{id}")
    public Response<DictKeyDetailDTO> getById(@PathVariable Long id) {
        return Response.success(adminDictService.getById(id));
    }

    @PostMapping
    public Response<DictKeyDetailDTO> createKey(@Valid @RequestBody DictKeySaveRequestDTO request) {
        return Response.success(adminDictService.createKey(request));
    }

    @PutMapping("/{id}")
    public Response<DictKeyDetailDTO> updateKey(@PathVariable Long id,
            @Valid @RequestBody DictKeySaveRequestDTO request) {
        return Response.success(adminDictService.updateKey(id, request));
    }

    @DeleteMapping("/{id}")
    public Response<Void> deleteKey(@PathVariable Long id) {
        adminDictService.deleteKey(id);
        return Response.success();
    }

    @PostMapping("/{id}/values")
    public Response<DictValueAdminDTO> createValue(@PathVariable Long id,
            @Valid @RequestBody DictValueSaveRequestDTO request) {
        return Response.success(adminDictService.createValue(id, request));
    }

    @PutMapping("/values/{valueId}")
    public Response<DictValueAdminDTO> updateValue(@PathVariable Long valueId,
            @Valid @RequestBody DictValueSaveRequestDTO request) {
        return Response.success(adminDictService.updateValue(valueId, request));
    }

    @DeleteMapping("/values/{valueId}")
    public Response<Void> deleteValue(@PathVariable Long valueId) {
        adminDictService.deleteValue(valueId);
        return Response.success();
    }

    @PostMapping("/cache/reload")
    public Response<Void> reloadCache() {
        adminDictService.reloadCache();
        return Response.success();
    }
}
