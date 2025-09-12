package com.pado.domain.study.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }

    public MemberNotFoundException(String detail) {
        super(ErrorCode.MEMBER_NOT_FOUND, detail);
    }
}