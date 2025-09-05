package com.pado.domain.user.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException() {
        super(ErrorCode.DUPLICATE_NICKNAME);
    }
    public DuplicateNicknameException(String detail) {
        super(ErrorCode.DUPLICATE_NICKNAME, detail);
    }
}
