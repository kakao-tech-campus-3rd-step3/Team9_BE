package com.pado.domain.quiz.dto.request;

import java.util.List;

public record QuizSubmissionRequestDto(
        List<AnswerRequestDto> answers
) {

}
