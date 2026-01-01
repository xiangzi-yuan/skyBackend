package com.sky.exception;

/**
 * 业务异常（继承BaseException以便被全局异常处理器捕获）
 */
public class BusinessException extends BaseException {
    public BusinessException() {
    }

    public BusinessException(String msg) {
        super(msg);
    }
}
