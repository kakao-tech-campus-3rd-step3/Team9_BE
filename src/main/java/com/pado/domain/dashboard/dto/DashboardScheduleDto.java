package com.pado.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "대시보드 일정 정보 DTO")
public record DashboardScheduleDto(
        @Schema(description = "일정 제목", example = "3차 스터디 모임")
        String title,

        @Schema(description = "일정 시작 시간", example = "2025-09-03T10:00:00")
        LocalDateTime start_time,

        @Schema(description = "일정 종료 시간", example = "2025-09-03T12:00:00")
        LocalDateTime end_time
) {}