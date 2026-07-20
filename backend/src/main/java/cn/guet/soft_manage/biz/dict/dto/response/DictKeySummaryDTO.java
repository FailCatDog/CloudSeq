package cn.guet.soft_manage.biz.dict.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictKeySummaryDTO {

    private Long id;

    private String keyCode;

    private String keyName;

    private String remark;

    private Integer valueCount;

    private Long version;
}
