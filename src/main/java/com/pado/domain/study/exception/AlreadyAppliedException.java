package com.pado.domain.study.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class AlreadyAppliedException extends BusinessException {

    public AlreadyAppliedException() {
        super(ErrorCode.ALREADY_APPLIED);
    }

    public AlreadyAppliedException(String detail) {
        super(ErrorCode.ALREADY_APPLIED, detail);
    }
}