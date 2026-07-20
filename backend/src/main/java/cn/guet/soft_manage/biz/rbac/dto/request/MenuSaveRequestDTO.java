package cn.guet.soft_manage.biz.rbac.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuSaveRequestDTO {

    private Long parentId;

    @NotBlank
    @Size(max = 64)
    private String menuName;

    @NotBlank
    @Size(max = 8)
    private String menuType;

    @Size(max = 128)
    private String path;

    @Size(max = 16)
    private String routeMatch;

    @Size(max = 128)
    private String component;

    @Size(max = 128)
    private String perms;

    @Size(max = 16)
    private String apiMethod;

    @Size(max = 256)
    private String apiPath;

    @Size(max = 64)
    private String icon;

    private Integer sort;

    @Size(max = 16)
    private String visible;

    @Size(max = 16)
    private String status;

    @Size(max = 256)
    private String remark;

    private Long version;
}
