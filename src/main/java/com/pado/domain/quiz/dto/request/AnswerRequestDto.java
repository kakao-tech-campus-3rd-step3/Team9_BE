package com.pado.domain.quiz.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "단일 문제 답안 DTO")
public record AnswerRequestDto(
        @JsonProperty("question_id")
        @Schema(description = "문제 ID", example = "1")
        Long questionId,

        @JsonProperty("user_answer")
        @Schema(description = "사용자가 선택한 선지 ID 또는 작성한 정답", example = "정답 텍스트")
        String userAnswer
) {

}