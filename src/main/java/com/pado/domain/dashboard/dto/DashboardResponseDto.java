package com.pado.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "메인 대시보드 응답 DTO")
public record DashboardResponseDto(
        @Schema(description = "참여 중인 스터디 목록")
        List<DashboardStudyDto> studies
) {}