package com.pado.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 대시보드 조회 응답 DTO")
public record StudyDashboardResponseDto(
        @Schema(description = "스터디 제목")
        String study_title,

        @Schema(description = "가장 최근 공지사항")
        LatestNoticeDto latest_notice,

        @Schema(description = "가장 가까운 예정된 스터디 일정")
        UpcomingScheduleDto upcoming_schedule
) {

}