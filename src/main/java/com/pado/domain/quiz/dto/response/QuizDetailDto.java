package com.pado.domain.quiz.dto.response;

import java.util.List;

public record QuizDetailDto(
        Long quizId,
        String title,
        Integer timeLimitSeconds,
        List<QuestionDetailDto> questions
) {

}