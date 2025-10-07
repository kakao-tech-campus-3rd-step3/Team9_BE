package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택지 결과 DTO")
public record ChoiceResultDto(
        @JsonProperty("choice_id")
        @Schema(description = "선택지 ID", example = "1")
        Long choiceId,

        @JsonProperty("choice_text")
        @Schema(description = "선택지 내용", example = "extends")
        String choiceText,

        @JsonProperty("is_correct_answer")
        @Schema(description = "정답 여부", example = "true")
        boolean isCorrectAnswer,

        @JsonProperty("was_user_choice")
        @Schema(description = "사용자가 선택했는지 여부", example = "true")
        boolean wasUserChoice
) {

}