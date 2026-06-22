package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-06-18
 * @Description: 协同令牌响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabTokenResponseDTO {

    private String token;

    private String wsUrl;

    private String room;

    private Boolean canWrite;

    private String displayName;
}
