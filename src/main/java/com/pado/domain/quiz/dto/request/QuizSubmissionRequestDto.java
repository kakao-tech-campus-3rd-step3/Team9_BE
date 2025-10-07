package com.pado.domain.quiz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "답안 제출 DTO")
public record QuizSubmissionRequestDto(
        @Schema(description = "사용자가 제출한 답안 목록")
        List<AnswerRequestDto> answers
) {

}