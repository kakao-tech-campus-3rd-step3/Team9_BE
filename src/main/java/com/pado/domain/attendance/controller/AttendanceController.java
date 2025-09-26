package com.pado.domain.attendance.controller;

import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.attendance.dto.AttendanceStatusDto;
import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.attendance.dto.MemberAttendanceDto;
import com.pado.domain.attendance.service.AttendanceService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.schedule.Api404ScheduleNotFoundError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Tag(name = "10. Attendance", description = "출석 관리 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
        summary = "전체 참여 현황 조회",
        description = "지금까지 진행한 스터디의 전체 참여 현황을 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
        responseCode = "200", description = "전체 참여 현황 조회 성공",
        content = @Content(schema = @Schema(implementation = AttendanceListResponseDto.class))
    )
    @Parameters({
        @Parameter(name = "study_id", description = "참여 현황을 조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/studies/{study_id}/attendance")
    public ResponseEntity<AttendanceListResponseDto> getFullAttendance(
        @PathVariable("study_id") Long studyId
    ) {
        return ResponseEntity.ok(attendanceService.getFullAttendance(studyId));
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404ScheduleNotFoundError
    @Operation(
        summary = "개별 참여 현황 조회",
        description = "사용자의 특정 스터디 일정에 대한 참여 현황을 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
        responseCode = "200", description = "개별 참여 현황 조회 성공",
        content = @Content(schema = @Schema(implementation = AttendanceStatusResponseDto.class))
    )
    @Parameters({
        @Parameter(name = "schedule_id", description = "출석 상태를 조회할 일정의 ID", required = true, example = "1234")
    })
    @GetMapping("schedules/{scheduleid}/attendance")
    public ResponseEntity<AttendanceStatusResponseDto> getIndividualAttendanceStatus(
        @PathVariable("scheduleid") Long scheduleId,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(attendanceService.getIndividualAttendanceStatus(scheduleId, user));
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404ScheduleNotFoundError
    @Operation(
        summary = "출석 체크",
        description = "스터디원이 자신의 출석을 체크합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "출석 체크 성공",
            content = @Content(schema = @Schema(implementation = AttendanceStatusResponseDto.class))),
        @ApiResponse(responseCode = "409", description = "출석 시간 만료 또는 이미 출석 체크됨",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = {
                    @ExampleObject(
                        name = "이미 출석 체크됨 예시",
                        value = """
                            {
                              "code": "ALREADY_CHECKED_IN",
                              "message": "이미 출석 체크되었습니다.",
                              "errors": [],
                              "timestamp": "2025-09-07T08:15:10.8668626",
                              "path": "/api/schedules/1234/attendance"
                            }
                            """
                    )
                })
        )
    })
    @Parameters({
        @Parameter(name = "schedule_id", description = "출석 체크할 일정의 ID", required = true, example = "1234")
    })
    @PostMapping("/schedules/{schedule_id}/attendance")
    public ResponseEntity<AttendanceStatusResponseDto> checkAttendance(
        @PathVariable("schedule_id") Long scheduleId,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(attendanceService.checkIn(scheduleId, user));
    }
}
