package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택지 DTO")
public record ChoiceDto(
        @JsonProperty("choice_id")
        @Schema(description = "선택지 ID", example = "1")
        Long choiceId,

        @JsonProperty("choice_text")
        @Schema(description = "선택지 내용", example = "extends")
        String choiceText
) {

}