package cn.guet.soft_manage.biz.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 协同服务持久化请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabPersistRequestDTO {

    private Long nodeId;

    private String yjsStateBase64;

    private String contentMd;

    private Long version;

    private Long updateUser;
}
