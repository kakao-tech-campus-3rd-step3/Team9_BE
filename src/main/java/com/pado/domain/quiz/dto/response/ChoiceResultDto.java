package com.pado.domain.quiz.dto.response;

public record ChoiceResultDto(
        Long choiceId,
        String choiceText,
        boolean isCorrectAnswer,
        boolean wasUserChoice
) {

}