package cn.guet.soft_manage.frame.exception;

import cn.guet.soft_manage.frame.common.Response;
import cn.guet.soft_manage.frame.enums.BizResponseCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Response<Void> handleBusinessException(BusinessException ex) {
        log.error("业务异常", ex);
        return Response.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常", ex);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Response.fail(BizResponseCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    public Response<Void> handleBindException(BindException ex) {
        log.error("参数绑定异常", ex);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Response.fail(BizResponseCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Response<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        log.error("约束校验异常", ex);
        return Response.fail(BizResponseCode.PARAM_ERROR, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Response<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("请求体解析异常", ex);
        return Response.fail(BizResponseCode.REQUEST_BODY_INVALID);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public Response<Void> handleExpiredJwtException(ExpiredJwtException ex) {
        log.warn("JWT 已过期", ex);
        return Response.fail(BizResponseCode.TOKEN_EXPIRED);
    }

    @ExceptionHandler(JwtException.class)
    public Response<Void> handleJwtException(JwtException ex) {
        log.warn("JWT 无效", ex);
        return Response.fail(BizResponseCode.TOKEN_INVALID);
    }

    @ExceptionHandler(Exception.class)
    public Response<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Response.fail(BizResponseCode.SYSTEM_ERROR);
    }
}
