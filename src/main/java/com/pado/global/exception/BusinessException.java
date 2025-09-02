package com.pado.global.exception;

import com.pado.global.exception.ErrorCode;

public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode code) {
        super(code.message);      // 기본 메시지
        this.errorCode = code;
    }

    public BusinessException(ErrorCode code, String detail) {
        super(detail != null ? detail : code.message);  // 상세 메시지로 덮어쓰기 가능
        this.errorCode = code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
