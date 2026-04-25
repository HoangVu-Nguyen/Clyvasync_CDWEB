package com.commoncore.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ResultCode resultCode;

    public AppException(ResultCode resultCode) {
        super(resultCode.name());
        this.resultCode = resultCode;
    }
}