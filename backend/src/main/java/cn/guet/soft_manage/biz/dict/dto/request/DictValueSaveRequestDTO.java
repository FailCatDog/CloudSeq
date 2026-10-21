package cn.guet.soft_manage.biz.dict.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictValueSaveRequestDTO {

    @NotBlank
    @Size(max = 64)
    private String valueCode;

    @NotBlank
    @Size(max = 64)
    private String valueName;

    @Size(max = 256)
    private String remark;

    private Integer sort;

    private Long version;
}
