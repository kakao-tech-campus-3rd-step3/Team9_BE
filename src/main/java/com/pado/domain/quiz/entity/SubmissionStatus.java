package com.pado.domain.quiz.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubmissionStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("제출완료");

    private final String label;

    public static String toLabel(SubmissionStatus status) {
        if (status == null) {
            return "미응시";
        }
        return status.getLabel();
    }
}