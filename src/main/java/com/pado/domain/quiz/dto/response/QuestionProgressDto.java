package com.pado.domain.quiz.dto.response;

import java.util.List;

public record QuestionProgressDto(
        Long questionId,
        String questionType,
        String questionText,
        List<ChoiceDto> choices,
        String userAnswer
) {

}