package com.freedoc.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final Object[] args;

    public BusinessException(String messageKey) {
        super(messageKey);
        this.code = 500;
        this.args = new Object[0];
    }

    public BusinessException(int code, String messageKey) {
        super(messageKey);
        this.code = code;
        this.args = new Object[0];
    }

    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.code = 500;
        this.args = args;
    }

    public BusinessException(int code, String messageKey, Object... args) {
        super(messageKey);
        this.code = code;
        this.args = args;
    }

}
