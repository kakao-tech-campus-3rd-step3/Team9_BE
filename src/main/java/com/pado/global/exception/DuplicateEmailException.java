package com.pado.global.exception;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
    public DuplicateEmailException(String detail) {
        super(ErrorCode.DUPLICATE_EMAIL, detail);
    }
}
