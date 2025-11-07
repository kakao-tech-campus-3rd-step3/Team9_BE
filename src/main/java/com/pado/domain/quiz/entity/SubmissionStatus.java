package com.pado.domain.quiz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionStatus {
    NOT_TAKEN("미응시"),
    IN_PROGRESS("진행중"),
    COMPLETED("제출완료");

    private final String label;

    public String getName() {
        return name();
    }

    public static SubmissionStatus from(String statusString) {
        if (statusString == null) {
            return NOT_TAKEN;
        }

        try {
            return SubmissionStatus.valueOf(statusString);
        } catch (IllegalArgumentException e) {
            return NOT_TAKEN;
        }
    }
}