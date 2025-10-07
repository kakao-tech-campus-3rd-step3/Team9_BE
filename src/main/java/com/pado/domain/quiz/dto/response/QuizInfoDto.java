package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.entity.QuizStatus;
import com.pado.domain.quiz.entity.SubmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "퀴즈 정보 DTO")
public record QuizInfoDto(
        @JsonProperty("quiz_id")
        @Schema(description = "퀴즈 ID", example = "1")
        Long quizId,

        @Schema(description = "퀴즈 제목", example = "자바 기초 1주차 퀴즈")
        String title,

        @JsonProperty("created_by")
        @Schema(description = "퀴즈 생성자", example = "홍길동")
        String createdBy,

        @JsonProperty("question_count")
        @Schema(description = "문제 개수", example = "10")
        int questionCount,

        @JsonProperty("time_limit_seconds")
        @Schema(description = "퀴즈 제한 시간(초)", example = "600")
        Integer timeLimitSeconds,

        @JsonProperty("quiz_status")
        @Schema(description = "퀴즈 상태", example = "ACTIVE")
        QuizStatus quizStatus,

        @JsonProperty("submission_status")
        @Schema(description = "사용자 제출 상태 ENUM(NOT_TAKEN/IN_PROGRESS/COMPLETED", example = "COMPLETED")
        SubmissionStatus submissionStatus,

        @JsonProperty("score")
        @Schema(description = "맞춘 문제 수 (제출 완료 상태일 때만 존재)", example = "7")
        Integer score,

        @JsonProperty("submission_id")
        @Schema(description = "제출 기록 ID (답안지가 있는 경우에만 존재)", example = "123")
        Long submissionId

)  implements CursorIdentifiable {

    @Override
    @JsonIgnore
    public Long getCursorId() {
        return this.quizId;
    }

    public static QuizInfoDto of(QuizInfoProjection proj, int questionCount, SubmissionStatusDto submissionInfo) {
        SubmissionStatus status;
        Integer score = null;
        Long submissionId = null;

        if (submissionInfo == null) {
            status = SubmissionStatus.NOT_TAKEN;
        } else {
            status = submissionInfo.status();
            submissionId = submissionInfo.submissionId();
            if (status == SubmissionStatus.COMPLETED) {
                score = submissionInfo.score();
            }
        }

        return new QuizInfoDto(
                proj.quizId(),
                proj.title(),
                proj.createdBy(),
                questionCount,
                proj.timeLimitSeconds(),
                proj.quizStatus(),
                status,
                score,
                submissionId
        );
    }
}