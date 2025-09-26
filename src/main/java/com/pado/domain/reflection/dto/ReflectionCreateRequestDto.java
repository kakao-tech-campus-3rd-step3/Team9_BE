package com.pado.domain.reflection.dto;

import jakarta.validation.constraints.*;

public record ReflectionCreateRequestDto(
    Long scheduleId,
    @NotNull Integer satisfactionScore,
    @NotNull Integer understandingScore,
    @NotNull Integer participationScore,
    @NotBlank String learnedContent,
    @NotBlank String improvement
) {

}