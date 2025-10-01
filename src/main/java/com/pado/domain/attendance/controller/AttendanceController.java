package com.pado.domain.attendance.controller;

import com.pado.domain.attendance.dto.*;
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

import javax.net.ssl.HttpsURLConnection;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Tag(name = "14. Attendance", description = "출석 관리 관련 API")
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
    @GetMapping("/studies/{study_id}/attendances")
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
    @GetMapping("/schedules/{schedule_id}/me")
    public ResponseEntity<AttendanceStatusResponseDto> getMyAttendanceStatus(
        @PathVariable("schedule_id") Long scheduleId,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(attendanceService.getMyAttendanceStatus(scheduleId, user));
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "특정 스케줄 참여 현황 조회",
            description = "특정 스케줄의 스터디원 참여 현황을 조회합니다. (스터디 리더만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "전체 참여 현황 조회 성공",
            content = @Content(schema = @Schema(implementation = AttendanceListResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "study_id", description = "참여 현황을 조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/studies/{study_id}/attendances/{schedule_id}")
    public ResponseEntity<AttendanceListResponseDto> getScheduleAttendance(
            @PathVariable("study_id") Long studyId,
            @PathVariable("schedule_id") Long scheuleId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(attendanceService.getScheduleAttendance(studyId, scheuleId, user));
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404ScheduleNotFoundError
    @Operation(
            summary = "참석 여부 수정",
            description = "스터디 리더가 스터디원의 참석 여부를 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참석 수정 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceStatusResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @Parameters({
            @Parameter(name = "schedule_id", description = "참석 여부 수정할 일정의 ID", required = true, example = "1234")
    })
    @PatchMapping("/schedules/{schedule_id}/attendances")
    public ResponseEntity<AttendanceStatusResponseDto> changeAttendance(
            @PathVariable("schedule_id") Long scheduleId,
            @RequestBody AttendanceMemberStatusRequestDto attendanceMemberStatusRequestDto,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        attendanceService.changeMemberAttendanceStatus(scheduleId, attendanceMemberStatusRequestDto, user);
        return ResponseEntity.status(HttpsURLConnection.HTTP_OK).build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404ScheduleNotFoundError
    @Operation(
        summary = "참석 여부 추가",
        description = "스터디원이 자신의 참석 여부를 추가합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "참석 추가 성공",
            content = @Content(schema = @Schema(implementation = AttendanceStatusResponseDto.class)))
    })
    @Parameters({
        @Parameter(name = "schedule_id", description = "참석 여부 추가할 일정의 ID", required = true, example = "1234")
    })
    @PatchMapping("/schedules/{schedule_id}/attendances/me")
    public ResponseEntity<Void> addAttendance(
        @PathVariable("schedule_id") Long scheduleId,
        @RequestBody AttendanceStatusRequestDto  attendanceStatusRequestDto,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        attendanceService.changeMyAttendanceStatus(scheduleId, attendanceStatusRequestDto, user);
        return ResponseEntity.status(HttpsURLConnection.HTTP_OK).build();
    }
}
