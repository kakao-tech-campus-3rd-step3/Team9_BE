package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "퀴즈 진행 상태 DTO")
public record QuizProgressDto(
        @JsonProperty("submission_id")
        @Schema(description = "답안지 ID", example = "1")
        Long submissionId,

        @JsonProperty("quiz_title")
        @Schema(description = "퀴즈 제목", example = "자바 기초 1주차 퀴즈")
        String quizTitle,

        @JsonProperty("time_limit_seconds")
        @Schema(description = "총 제한 시간(초)", example = "600")
        Integer timeLimitSeconds,

        @JsonProperty("remaining_seconds")
        @Schema(description = "남은 시간(초)", example = "300")
        Long remainingSeconds,

        @Schema(description = "진행 중인 문제 목록")
        List<QuestionProgressDto> questions
) {

}
