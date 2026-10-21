package cn.guet.soft_manage.biz.dict.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-27
 * @Description: 字典项对外 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictValueDTO {

    private String valueCode;

    private String valueName;

    private String remark;

    private Integer sort;
}
