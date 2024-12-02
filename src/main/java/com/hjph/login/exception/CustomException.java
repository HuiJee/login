package com.hjph.login.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    protected ErrorCode errorCode;

    public CustomException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

