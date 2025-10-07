package com.pado.domain.quiz.event;

public record QuizCompletedEvent(
        Long studyId,
        String quizTitle,
        Long quizId
) {
}