package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 协同冷启动数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabLoadResponseDTO {

    private Long nodeId;

    private String contentMd;

    private String yjsStateBase64;

    private Long version;
}
