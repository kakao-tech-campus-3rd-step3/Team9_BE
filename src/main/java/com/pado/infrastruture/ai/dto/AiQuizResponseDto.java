package com.pado.infrastruture.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiQuizResponseDto(
        @JsonProperty("recommendedTimeLimitSeconds")
        Integer recommendedTimeLimitSeconds,

        @JsonProperty("questions")
        List<AiQuestionDto> questions
) {}
