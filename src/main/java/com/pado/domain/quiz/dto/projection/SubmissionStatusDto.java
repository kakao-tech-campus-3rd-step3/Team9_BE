package com.pado.domain.quiz.dto.projection;

import com.pado.domain.quiz.entity.SubmissionStatus;
import com.querydsl.core.annotations.QueryProjection;

public record SubmissionStatusDto(
        Long quizId,
        SubmissionStatus status,
        Integer score
) {

    @QueryProjection
    public SubmissionStatusDto(Long quizId, SubmissionStatus status, Integer score) {
        this.quizId = quizId;
        this.status = status;
        this.score = score;
    }
}
