package com.pado.domain.quiz.dto.response;

import java.util.List;

public record QuizProgressDto(
        Long submissionId,
        String quizTitle,
        Integer timeLimitSeconds,
        Long remainingSeconds,
        List<QuestionProgressDto> questions
) {

}

