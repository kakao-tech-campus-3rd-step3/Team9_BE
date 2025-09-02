package com.pado.domain.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "출석 상태 DTO")
public record AttendanceStatusDto(
        @Schema(description = "출석 상태", example = "true")
        boolean status,

        @Schema(description = "일정 날짜", example = "2025-09-02T10:00:00")
        LocalDateTime schedule_date
) {}
