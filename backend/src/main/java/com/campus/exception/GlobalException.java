package com.campus.exception;

public class GlobalException extends RuntimeException {
    private Integer code;

    public GlobalException(String message) {
        super(message);
        this.code = 500;
    }

    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}