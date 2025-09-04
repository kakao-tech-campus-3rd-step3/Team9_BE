package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "조율 중인 일정 응답 DTO")
public record ScheduleTuneResponseDto(
        @Schema(description = "일정 제목", example = "3차 스터디 모임 날짜 조율")
        String title,

        @Schema(description = "일정 시작 시간", example = "2025-09-10T14:00:00")
        LocalDateTime start_time,

        @Schema(description = "일정 종료 시간", example = "2025-09-10T16:00:00")
        LocalDateTime end_time
) {}