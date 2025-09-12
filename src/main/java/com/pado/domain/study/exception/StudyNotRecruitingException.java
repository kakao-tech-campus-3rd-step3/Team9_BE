package com.pado.domain.study.exception;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public class StudyNotRecruitingException extends BusinessException {

    public StudyNotRecruitingException() {
        super(ErrorCode.STUDY_NOT_RECRUITING);
    }

    public StudyNotRecruitingException(String detail) {
        super(ErrorCode.STUDY_NOT_RECRUITING, detail);
    }
}