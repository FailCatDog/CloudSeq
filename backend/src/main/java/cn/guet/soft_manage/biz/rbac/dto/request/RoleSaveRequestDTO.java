package cn.guet.soft_manage.biz.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleSaveRequestDTO {

    @NotBlank
    @Size(max = 32)
    private String roleKey;

    @NotBlank
    @Size(max = 64)
    private String roleName;

    private Integer roleSort;

    @NotBlank
    @Size(max = 16)
    private String dataScope;

    @Size(max = 128)
    private String homePath;

    @Size(max = 16)
    private String status;

    @Size(max = 256)
    private String remark;

    private Long version;
}
