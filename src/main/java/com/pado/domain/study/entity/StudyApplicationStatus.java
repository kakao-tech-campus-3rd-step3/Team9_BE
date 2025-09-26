package com.pado.domain.study.entity;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;

public enum StudyApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static StudyApplicationStatus fromString(String statusStr) {
        if (statusStr == null) {
            throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE);
        }

        for (StudyApplicationStatus status : StudyApplicationStatus.values()) {
            if (status.name().equalsIgnoreCase(statusStr)) {
                return status;
            }
        }

        throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE);
    }
}