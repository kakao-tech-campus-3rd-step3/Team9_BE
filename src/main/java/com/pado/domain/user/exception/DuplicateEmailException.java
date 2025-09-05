package com.pado.domain.user.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
    public DuplicateEmailException(String detail) {
        super(ErrorCode.DUPLICATE_EMAIL, detail);
    }
}
