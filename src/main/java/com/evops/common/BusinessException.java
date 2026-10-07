package com.evops.common;

/** 业务校验失败异常，由 GlobalExceptionHandler 统一转成失败响应 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
