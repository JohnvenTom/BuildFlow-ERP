package com.buildflow.erp.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于业务逻辑校验不通过时抛出，由全局异常处理器统一捕获返回前端
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误状态码 */
    private final int code;

    /**
     * 构造业务异常（默认500状态码）
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /**
     * 构造业务异常（自定义状态码）
     * @param code 错误状态码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 根据错误码枚举构造业务异常
     * @param errorCode 错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
}
