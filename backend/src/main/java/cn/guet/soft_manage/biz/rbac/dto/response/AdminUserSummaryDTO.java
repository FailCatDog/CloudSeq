package cn.guet.soft_manage.biz.rbac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryDTO {

    private Long id;

    private String username;

    private String realName;

    private String nickName;

    private String studentNo;

    private String role;

    private Integer isActive;

    private List<RoleSummaryDTO> roles;
}
