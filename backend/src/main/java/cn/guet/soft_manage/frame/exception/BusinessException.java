package cn.guet.soft_manage.frame.exception;

import cn.guet.soft_manage.frame.enums.BizResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 自定义业务异常
 */
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(BizResponseCode code) {
        this(code.getCode(), code.getMessage());
    }

    public BusinessException(String message) {
        this(BizResponseCode.SYSTEM_ERROR.getCode(), message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

}
