package com.cjc.exception;

/**
 * 自定义的异常类
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
