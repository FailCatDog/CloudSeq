package cn.guet.soft_manage.biz.rbac.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleAssignRequestDTO {

    @NotEmpty
    private List<Long> roleIds;
}
