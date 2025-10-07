package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "퀴즈 결과 DTO")
public record QuizResultDto(
        @JsonProperty("submission_id")
        @Schema(description = "답안지 ID", example = "1")
        Long submissionId,

        @Schema(description = "맞힌 문제 개수", example = "7")
        int score,

        @JsonProperty("total_questions")
        @Schema(description = "총 문제 수", example = "10")
        int totalQuestions,

        @Schema(description = "문제별 결과 목록")
        List<AnswerResultDto> results
) {

}