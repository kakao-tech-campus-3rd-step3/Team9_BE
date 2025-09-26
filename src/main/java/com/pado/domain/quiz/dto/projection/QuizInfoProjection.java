package com.pado.domain.quiz.dto.projection;

import com.pado.domain.quiz.entity.QuizStatus;
import com.querydsl.core.annotations.QueryProjection;

public record QuizInfoProjection (
    Long quizId,
    String title,
    String createdBy,
    Integer timeLimitSeconds,
    QuizStatus quizStatus

) {

    @QueryProjection
    public QuizInfoProjection(Long quizId, String title, String createdBy, Integer timeLimitSeconds, QuizStatus quizStatus) {
        this.quizId = quizId;
        this.title = title;
        this.createdBy = createdBy;
        this.timeLimitSeconds = timeLimitSeconds;
        this.quizStatus = quizStatus;
    }
}