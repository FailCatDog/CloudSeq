package cn.guet.soft_manage.biz.rbac.service;

import cn.guet.soft_manage.biz.rbac.dto.request.MenuSaveRequestDTO;
import cn.guet.soft_manage.biz.rbac.dto.response.AdminMenuNodeDTO;

import java.util.List;

public interface IAdminMenuService {

    List<AdminMenuNodeDTO> listTree();

    AdminMenuNodeDTO getById(Long id);

    AdminMenuNodeDTO create(MenuSaveRequestDTO request);

    AdminMenuNodeDTO update(Long id, MenuSaveRequestDTO request);

    void delete(Long id);

    void updateStatus(Long id, String status);
}
