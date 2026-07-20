package cn.guet.soft_manage.biz.rbac.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoleMenuAssignRequestDTO {

    private List<Long> menuIds;
}
