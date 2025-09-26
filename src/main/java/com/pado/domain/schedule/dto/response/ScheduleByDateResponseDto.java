package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "날짜별 스터디 일정 조회 응답 DTO")
public record ScheduleByDateResponseDto (
        @Schema(description = "일정 ID", example = "101")
        Long schedule_id,

        @Schema(description = "스터디 ID", example = "1")
        Long study_id,

        @Schema(description = "일정 제목", example = "JPA 정기 스터디")
        String title,

        @Schema(description = "일정 시작 시간", example = "2025-09-16T19:00:00")
        LocalDateTime start_time,

        @Schema(description = "일정 종료 시간", example = "2025-09-16T21:00:00")
        LocalDateTime end_time
){

}