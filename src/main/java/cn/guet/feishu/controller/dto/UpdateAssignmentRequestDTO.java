package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateAssignmentRequestDTO {
    /**
     * 个人完成状态
     */
    private Integer status;
    /**
     * 个人进度百分比
     */
    private Integer progress;
    /**
     * 个人实际工时
     */
    private BigDecimal actualHours;
}

