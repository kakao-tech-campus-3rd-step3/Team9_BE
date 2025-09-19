package com.pado.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "다가오는 일정 정보")
public record UpcomingScheduleDto(
        @Schema(description = "일정 ID")
        Long schedule_id,

        @Schema(description = "일정 제목")
        String title,

        @Schema(description = "시작 시간")
        LocalDateTime start_time,

        @Schema(description = "D-day, 오늘 기준 D-day (오늘이면 D-0)")
        long d_day,

        @Schema(description = "현재 일정 참여자 수")
        int participant_count,

        @Schema(description = "총 스터디 멤버 수")
        int total_member_count
) {

}