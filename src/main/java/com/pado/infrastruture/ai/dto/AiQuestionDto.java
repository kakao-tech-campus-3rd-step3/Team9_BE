package com.pado.infrastruture.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiQuestionDto(
        @JsonProperty("questionType")
        String questionType,

        @JsonProperty("questionText")
        String questionText,

        @JsonProperty("options")
        List<String> options,

        @JsonProperty("correctAnswerIndex")
        Integer correctAnswerIndex,

        @JsonProperty("sampleAnswer")
        String sampleAnswer
) {

}