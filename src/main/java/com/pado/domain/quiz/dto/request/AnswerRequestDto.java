package com.pado.domain.quiz.dto.request;

public record AnswerRequestDto(
        Long questionId,
        String userAnswer
) {

}