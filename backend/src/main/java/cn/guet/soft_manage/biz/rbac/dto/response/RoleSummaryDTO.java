package cn.guet.soft_manage.biz.rbac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleSummaryDTO {

    private Long id;

    private String roleKey;

    private String roleName;

    private Integer roleSort;

    private String dataScope;

    private String homePath;

    private String status;

    private String remark;

    private Long version;
}
