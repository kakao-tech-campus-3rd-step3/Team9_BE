package com.pado.domain.study.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class AlreadyMemberException extends BusinessException {

    public AlreadyMemberException() {
        super(ErrorCode.ALREADY_MEMBER);
    }

    public AlreadyMemberException(String detail) {
        super(ErrorCode.ALREADY_MEMBER, detail);
    }
}