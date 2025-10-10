package com.pado.domain.quiz.dto.response;

import java.util.List;

public record QuizResultDto(
        Long submissionId,
        int score,
        int totalQuestions,
        List<AnswerResultDto> results
) {

}