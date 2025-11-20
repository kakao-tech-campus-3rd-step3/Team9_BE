package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pado.domain.quiz.entity.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대시보드용 퀴즈 정보 DTO")
public record QuizDashboardDto(

        @JsonProperty("quiz_id")
        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @JsonProperty("quiz_title")
        @Schema(description = "퀴즈 제목", example = "자바 기초 1주차 퀴즈")
        String quizTitle,

        @JsonProperty("submission_status")
        @Schema(description = "사용자 제출 상태 ENUM(NOT_TAKEN/IN_PROGRESS/COMPLETED)", example = "NOT_TAKEN")
        SubmissionStatus submissionStatus
) {

}