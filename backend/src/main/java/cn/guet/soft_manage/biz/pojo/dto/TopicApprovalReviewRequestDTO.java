package cn.guet.soft_manage.biz.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-26
 * @Description: 选题审批处理请求
 */
@Data
public class TopicApprovalReviewRequestDTO {

    @NotNull(message = "审批ID不能为空")
    private Long approvalId;

    @NotNull(message = "审批状态不能为空")
    private String approvalStatus;

    private String rejectReason;
}
