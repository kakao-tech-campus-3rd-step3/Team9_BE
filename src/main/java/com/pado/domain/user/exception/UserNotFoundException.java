package com.pado.domain.user.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
    public UserNotFoundException(String detail) {
        super(ErrorCode.USER_NOT_FOUND, detail);
    }
}

