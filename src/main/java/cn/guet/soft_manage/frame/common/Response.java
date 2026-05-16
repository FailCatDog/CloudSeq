package cn.guet.soft_manage.frame.common;

import cn.guet.soft_manage.frame.enums.BizResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 统一返回格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {

    private int code;

    private String message;

    private T data;

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(BizResponseCode.SUCCESS.getCode(), BizResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> Response<T> success(String message, T data) {
        return new Response<>(BizResponseCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Response<T> fail(BizResponseCode code) {
        return new Response<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> Response<T> fail(BizResponseCode code, String message) {
        return new Response<>(code.getCode(), message, null);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<>(code, message, null);
    }

    public static <T> Response<T> fail(String message) {
        return new Response<>(BizResponseCode.SYSTEM_ERROR.getCode(), message, null);
    }
}
