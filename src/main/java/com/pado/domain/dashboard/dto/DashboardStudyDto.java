package com.pado.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "대시보드 스터디 정보 DTO")
public record DashboardStudyDto(
        @Schema(description = "스터디 ID", example = "1")
        Long study_id,

        @Schema(description = "스터디 제목", example = "스프링 웹 개발 스터디")
        String title,

        @Schema(description = "스터디 일정 목록")
        List<DashboardScheduleDto> schedules
) {}