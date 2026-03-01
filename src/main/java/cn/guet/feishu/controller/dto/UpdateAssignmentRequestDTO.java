package cn.guet.feishu.controller.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateAssignmentRequestDTO {
    private Integer status;
    private Integer progress;
    private BigDecimal actualHours;
}

