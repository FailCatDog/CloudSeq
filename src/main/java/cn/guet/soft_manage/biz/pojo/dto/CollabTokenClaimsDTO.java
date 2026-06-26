package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协同 JWT 载荷
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabTokenClaimsDTO {

    private Long userId;

    private Long nodeId;

    private Boolean canWrite;

    private String displayName;

    private String avatarUrl;
}
