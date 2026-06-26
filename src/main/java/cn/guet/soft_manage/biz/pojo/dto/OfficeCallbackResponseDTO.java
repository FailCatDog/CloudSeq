package cn.guet.soft_manage.biz.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OnlyOffice 回调响应（须返回 error: 0 表示成功）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeCallbackResponseDTO {

    private int error;

    public static OfficeCallbackResponseDTO success() {
        return new OfficeCallbackResponseDTO(0);
    }

    public static OfficeCallbackResponseDTO failure() {
        return new OfficeCallbackResponseDTO(1);
    }
}
