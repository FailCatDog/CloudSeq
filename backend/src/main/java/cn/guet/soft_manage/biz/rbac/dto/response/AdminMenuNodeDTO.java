package cn.guet.soft_manage.biz.rbac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminMenuNodeDTO {

    private Long id;

    private Long parentId;

    private String menuName;

    private String menuType;

    private String path;

    private String routeMatch;

    private String component;

    private String perms;

    private String apiMethod;

    private String apiPath;

    private String icon;

    private Integer sort;

    private String visible;

    private String status;

    private String remark;

    private Long version;

    @Builder.Default
    private List<AdminMenuNodeDTO> children = new ArrayList<>();
}
