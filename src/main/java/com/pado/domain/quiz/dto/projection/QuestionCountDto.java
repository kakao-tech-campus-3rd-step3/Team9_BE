package com.pado.domain.quiz.dto.projection;

import com.querydsl.core.annotations.QueryProjection;

public record QuestionCountDto (
    Long quizId,
    Long questionCount
) {
    @QueryProjection
    public QuestionCountDto(Long quizId, Long questionCount) {
        this.quizId = quizId;
        this.questionCount = questionCount;
    }
}