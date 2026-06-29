package cn.guet.soft_manage.biz.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-18
 * @Description: 选题提交请求
 */
@Data
public class TeamTopicSubmitRequestDTO {

    @NotNull(message = "小组ID不能为空")
    private Long teamId;

    @NotBlank(message = "选题标题不能为空")
    private String topicTitle;

    private String topicDesc;
}
