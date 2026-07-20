package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.request.RoleSaveRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.RoleSummaryDTO;

import java.util.List;

public interface IAdminRoleService {

    List<RoleSummaryDTO> list(String status);

    RoleSummaryDTO getById(Long id);

    RoleSummaryDTO create(RoleSaveRequestDTO request);

    RoleSummaryDTO update(Long id, RoleSaveRequestDTO request);

    void updateStatus(Long id, String status);

    List<Long> listMenuIds(Long roleId);

    void replaceMenuIds(Long roleId, List<Long> menuIds);
}
