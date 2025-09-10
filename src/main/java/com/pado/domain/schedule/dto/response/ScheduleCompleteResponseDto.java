package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "조율 완료 응답 DTO")
public record ScheduleCompleteResponseDto(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success
) {}
