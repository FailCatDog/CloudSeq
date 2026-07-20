package cn.guet.soft_manage.biz.dict.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictValueAdminDTO {

    private Long id;

    private Long dictKeyId;

    private String valueCode;

    private String valueName;

    private String remark;

    private Integer sort;

    private Long version;
}
