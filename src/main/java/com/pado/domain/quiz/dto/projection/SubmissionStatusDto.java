package com.pado.domain.quiz.dto.projection;

import com.pado.domain.quiz.entity.SubmissionStatus;
import com.querydsl.core.annotations.QueryProjection;

public record SubmissionStatusDto(
        Long quizId,
        Long submissionId,
        SubmissionStatus status,
        Integer score
) {

    @QueryProjection
    public SubmissionStatusDto(Long quizId, Long submissionId, SubmissionStatus status, Integer score) {
        this.quizId = quizId;
        this.submissionId = submissionId;
        this.status = status;
        this.score = score;
    }
}