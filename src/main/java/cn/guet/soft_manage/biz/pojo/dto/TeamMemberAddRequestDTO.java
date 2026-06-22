package cn.guet.soft_manage.biz.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 添加成员请求
 */
@Data
public class TeamMemberAddRequestDTO {

    @NotNull(message = "小组ID不能为空")
    private Long teamId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
