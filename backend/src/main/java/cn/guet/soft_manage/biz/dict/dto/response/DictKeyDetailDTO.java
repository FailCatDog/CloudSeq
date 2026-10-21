package cn.guet.soft_manage.biz.dict.dto.response;

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
public class DictKeyDetailDTO {

    private Long id;

    private String keyCode;

    private String keyName;

    private String remark;

    private Long version;

    @Builder.Default
    private List<DictValueAdminDTO> values = new ArrayList<>();
}
