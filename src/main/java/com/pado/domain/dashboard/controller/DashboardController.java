package com.pado.domain.dashboard.controller;

import com.pado.domain.dashboard.dto.*;
import com.pado.domain.dashboard.dto.DashboardResponseDto;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import com.pado.global.swagger.annotation.user.Api404UserNotFoundError;
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

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "11. Dashboard", description = "대시보드 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    // TODO: 서비스 레이어 종속성 주입

    @Api404UserNotFoundError
    @Operation(
            summary = "메인 대시보드",
            description = "현재 로그인된 사용자가 참여 중인 스터디 목록과 그 스터디의 일정을 함께 조회합니다."
    )
    @ApiResponse(
            responseCode = "200", description = "메인 대시보드 조회 성공",
            content = @Content(schema = @Schema(implementation = DashboardResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "user_id", description = "대시보드를 조회할 사용자의 ID", required = true, example = "1")
    })
    @GetMapping("/dashboard/{user_id}")
    public ResponseEntity<DashboardResponseDto> getMainDashboard(
            @PathVariable("user_id") Long userId
    ) {
        // TODO: 메인 대시보드 로직 구현
        List<DashboardStudyDto> mockStudies = List.of(
                new DashboardStudyDto(
                        1L,
                        "스프링 웹 개발 스터디",
                        List.of(
                                new DashboardScheduleDto(
                                        "3차 스터디 모임",
                                        LocalDateTime.of(2025, 9, 3, 10, 0),
                                        LocalDateTime.of(2025, 9, 3, 12, 0)
                                )
                        )
                ),
                new DashboardStudyDto(
                        2L,
                        "알고리즘 스터디",
                        List.of(
                                new DashboardScheduleDto(
                                        "코딩 테스트 대비",
                                        LocalDateTime.of(2025, 9, 5, 19, 0),
                                        LocalDateTime.of(2025, 9, 5, 21, 0)
                                )
                        )
                )
        );
        DashboardResponseDto mockResponse = new DashboardResponseDto(mockStudies);

        return ResponseEntity.ok(mockResponse);
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "스터디 대시보드",
            description = "스터디의 공지사항, 문서 목록, 최근 일정, 랭킹 정보를 종합하여 조회합니다."
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
        // TODO: 스터디 대시보드 로직 구현
        StudyDashboardResponseDto mockResponse = new StudyDashboardResponseDto(
                "이번 주 스터디는 302호에서 진행됩니다.",
                List.of("스터디 규칙", "1주차 발표자료", "2주차 회고록"),
                new RecentScheduleDto("4차 스터디 모임", LocalDateTime.of(2025, 9, 8, 10, 0)),
                "파도타기"
        );

        return ResponseEntity.ok(mockResponse);
    }
}