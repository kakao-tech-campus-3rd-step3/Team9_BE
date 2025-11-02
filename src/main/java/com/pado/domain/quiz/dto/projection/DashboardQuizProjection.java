package com.pado.domain.quiz.dto.projection;

import com.querydsl.core.annotations.QueryProjection;

public record DashboardQuizProjection (
        Long quizId,
        String title,
        String submissionStatusString
) {
    @QueryProjection
    public DashboardQuizProjection(Long quizId, String title, String submissionStatusString) {
        this.quizId = quizId;
        this.title = title;
        this.submissionStatusString = submissionStatusString;
    }
}