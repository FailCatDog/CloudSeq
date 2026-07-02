package cn.guet.soft_manage.biz.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 创建小组请求
 */
@Data
public class TeamCreateRequestDTO {

    @NotBlank(message = "小组名称不能为空")
    private String teamName;

    @NotNull(message = "组长用户ID不能为空")
    private Long leaderUserId;
}
