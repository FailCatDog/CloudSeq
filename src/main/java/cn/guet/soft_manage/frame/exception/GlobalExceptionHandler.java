package cn.guet.soft_manage.frame.exception;

import cn.guet.soft_manage.frame.enums.BizResponseCode;
import cn.guet.soft_manage.frame.common.Response;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * @Author: 黄光宇
 * @CreateTime: 2026-05-16
 * @Description: 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Response<Void> handleBusinessException(BusinessException ex) {
        return Response.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Response.fail(BizResponseCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    public Response<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Response.fail(BizResponseCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        return Response.fail(BizResponseCode.PARAM_ERROR, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return Response.fail(BizResponseCode.REQUEST_BODY_INVALID);
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception ex) {
        return Response.fail(BizResponseCode.SYSTEM_ERROR);
    }
}
