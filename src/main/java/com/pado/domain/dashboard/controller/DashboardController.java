package com.pado.domain.dashboard.controller;

import com.pado.domain.dashboard.dto.*;
import com.pado.domain.dashboard.service.DashboardService;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "11. Dashboard", description = "대시보드 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "스터디 대시보드",
            description = "스터디의 공지사항, 최근 일정을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200", description = "스터디 대시보드 조회 성공",
            content = @Content(schema = @Schema(implementation = StudyDashboardResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "study_id", description = "대시보드를 조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/studies/{study_id}/dashboard")
    public ResponseEntity<StudyDashboardResponseDto> getStudyDashboard(
            @PathVariable("study_id") Long studyId
    ) {
        StudyDashboardResponseDto response = dashboardService.getStudyDashboard(studyId);
        return ResponseEntity.ok(response);
    }
}