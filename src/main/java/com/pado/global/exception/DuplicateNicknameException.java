package com.pado.global.exception;

public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException() {
        super(ErrorCode.DUPLICATE_NICKNAME);
    }
    public DuplicateNicknameException(String detail) {
        super(ErrorCode.DUPLICATE_NICKNAME, detail);
    }
}
