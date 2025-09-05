package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "일정 정보 응답 DTO")
public record ScheduleResponseDto(
        @Schema(description = "일정 ID", example = "1234")
        Long schedule_id,

        @Schema(description = "일정 제목", example = "2차 스터디 모임")
        String title,

        @Schema(description = "일정 시작 시간", example = "2025-09-03T10:00:00")
        LocalDateTime start_time,

        @Schema(description = "일정 종료 시간", example = "2025-09-03T12:00:00")
        LocalDateTime end_time
) {}