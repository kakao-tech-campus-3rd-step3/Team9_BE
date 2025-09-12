package com.pado.domain.study.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class StudyFullException extends BusinessException {

    public StudyFullException() {
        super(ErrorCode.STUDY_FULL);
    }

    public StudyFullException(String detail) {
        super(ErrorCode.STUDY_FULL, detail);
    }
}
