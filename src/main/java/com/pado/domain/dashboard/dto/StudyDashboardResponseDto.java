package com.pado.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 대시보드 응답 DTO")
public record StudyDashboardResponseDto(
        @Schema(description = "스터디 공지사항", example = "이번 주 스터디는 302호에서 진행됩니다.")
        String notice,

        @Schema(description = "스터디 문서 제목 목록", example = "[\"스터디 규칙\", \"1주차 발표자료\", \"2주차 회고록\"]")
        List<String> document_titles,

        @Schema(description = "가장 최근의 확정된 일정")
        RecentScheduleDto recent_schedule,

        @Schema(description = "랭킹 1위 스터디원 닉네임", example = "파도타기")
        String ranking_top
) {}