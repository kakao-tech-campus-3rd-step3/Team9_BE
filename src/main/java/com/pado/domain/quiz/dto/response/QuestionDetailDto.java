package com.pado.domain.quiz.dto.response;

import com.pado.domain.quiz.entity.QuestionType;

import java.util.List;

public record QuestionDetailDto(
        Long questionId,
        QuestionType questionType,
        String questionText,
        List<ChoiceDto> choices
) {

}
