package com.pado.domain.quiz.dto.response;

import java.util.List;

public record AnswerResultDto(
        Long questionId,
        String questionText,
        boolean isCorrect,
        String userAnswer,
        String correctAnswer,
        List<ChoiceResultDto> choices
) {

}