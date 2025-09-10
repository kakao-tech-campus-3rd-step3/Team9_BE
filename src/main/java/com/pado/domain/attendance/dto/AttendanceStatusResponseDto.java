package com.pado.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개별 참여 현황 조회 응답 DTO")
public record AttendanceStatusResponseDto(
        @Schema(description = "출석 상태", example = "true")
        boolean status
) {}
