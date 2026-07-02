package cn.guet.soft_manage.biz.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协同持久化响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabPersistResponseDTO {

    private Long nodeId;

    private Long version;
}
