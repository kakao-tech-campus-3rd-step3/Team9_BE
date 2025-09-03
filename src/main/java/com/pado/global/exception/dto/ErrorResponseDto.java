package com.pado.global.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 에러 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        String error_code,
        String field,
        String message
) {}
