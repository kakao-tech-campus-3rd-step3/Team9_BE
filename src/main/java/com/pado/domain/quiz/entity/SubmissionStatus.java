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
}