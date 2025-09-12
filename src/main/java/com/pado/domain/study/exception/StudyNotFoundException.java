package com.pado.domain.study.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class StudyNotFoundException extends BusinessException {
    public StudyNotFoundException() {
        super(ErrorCode.STUDY_NOT_FOUND);
    }

    public StudyNotFoundException(String detail) {
        super(ErrorCode.STUDY_NOT_FOUND, detail);
    }
}