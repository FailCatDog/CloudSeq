package cn.guet.soft_manage.biz.dict.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DictKeySaveRequestDTO {

    @NotBlank
    @Size(max = 64)
    private String keyCode;

    @NotBlank
    @Size(max = 64)
    private String keyName;

    @Size(max = 256)
    private String remark;

    private Long version;
}
