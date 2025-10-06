package com.pado.domain.schedule.controller;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.PastScheduleResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleByDateResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleResponseDto;
import com.pado.domain.schedule.service.ScheduleService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.swagger.annotation.schedule.Api404ScheduleNotFoundError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "08. Schedule", description = "스터디 일정 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(summary = "스터디 전체 일정 조회", description = "스터디에 등록된 모든 일정을 조회합니다. (스터디 멤버만 가능)")
    @ApiResponse(responseCode = "200", description = "일정 목록 조회 성공",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = ScheduleResponseDto.class))))
    @Parameters({
        @Parameter(name = "study_id", description = "일정을 조회할 스터디의 ID", required = true, example = "1")})
    @GetMapping("/studies/{study_id}/schedules")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedules(
        @PathVariable("study_id") Long studyId) {
        List<ScheduleResponseDto> schedules = scheduleService.findAllSchedulesByStudyId(studyId);
        return ResponseEntity.ok(schedules);
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(summary = "개별 일정 세부 조회", description = "지정된 ID를 가진 일정의 상세 정보를 조회합니다. (스터디 멤버만 가능)")
    @ApiResponse(responseCode = "200", description = "일정 상세 정보 조회 성공", content = @Content(schema = @Schema(implementation = ScheduleDetailResponseDto.class)))
    @Parameters({
        @Parameter(name = "schedule_id", description = "조회할 일정의 ID", required = true, example = "1234")})
    @GetMapping("/schedules/{schedule_id}")
    public ResponseEntity<ScheduleDetailResponseDto> getScheduleDetail(
        @PathVariable("schedule_id") Long scheduleId) {
        ScheduleDetailResponseDto scheduleDetail = scheduleService.findScheduleDetailById(
            scheduleId);
        return ResponseEntity.ok(scheduleDetail);
    }

    @Operation(summary = "내 스터디 월별 일정 조회", description = "로그인한 사용자가 속한 모든 스터디의 일정을 특정 연월 기준으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/schedules/me")
    public ResponseEntity<List<ScheduleByDateResponseDto>> getMySchedules(
        @Parameter(description = "조회할 연도", required = true, example = "2025") @RequestParam int year,
        @Parameter(description = "조회할 월", required = true, example = "9") @RequestParam int month,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        List<ScheduleByDateResponseDto> schedules = scheduleService.findMySchedulesByMonth(
            user.getId(), year, month);
        return ResponseEntity.ok(schedules);
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(summary = "일정 생성", description = "스터디에 새로운 일정을 생성합니다. (스터디 리더만 가능)")
    @ApiResponse(responseCode = "201", description = "일정 생성 성공", content = @Content)
    @Parameters({
        @Parameter(name = "study_id", description = "일정을 생성할 스터디의 ID", required = true, example = "1")})
    @PostMapping("/studies/{study_id}/schedules")
    public ResponseEntity<Void> createSchedule(@PathVariable("study_id") Long studyId,
        @Valid @RequestBody ScheduleCreateRequestDto request) {
        scheduleService.createSchedule(studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Api403ForbiddenStudyLeaderOnlyError
    @Api404ScheduleNotFoundError
    @Operation(summary = "일정 수정", description = "지정된 ID를 가진 일정을 수정합니다. (스터디 리더만 가능)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "일정 수정 성공", content = @Content)
    @Parameters({
        @Parameter(name = "schedule_id", description = "수정할 일정의 ID", required = true, example = "1234")
    })
    @PutMapping("schedules/{scheduleid}")
    public ResponseEntity<Void> updateSchedule(
        @PathVariable("scheduleid") Long scheduleId,
        @Valid @RequestBody ScheduleCreateRequestDto request
    ) {
        scheduleService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok().build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404ScheduleNotFoundError
    @Operation(summary = "일정 삭제", description = "지정된 ID를 가진 일정을 삭제합니다. (스터디 리더만 가능)")
    @ApiResponse(responseCode = "204", description = "일정 삭제 성공", content = @Content)
    @Parameters({
        @Parameter(name = "schedule_id", description = "삭제할 일정의 ID", required = true, example = "1")
    })
    @DeleteMapping("schedules/{scheduleid}")
    public ResponseEntity<Void> deleteSchedule(
        @PathVariable("scheduleid") Long scheduleId
    ) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(summary = "회고 작성 가능한 스터디 일정 조회", description = "회고 작성/수정 시 선택 가능한, 이미 지난 스터디 일정 목록을 조회합니다. (스터디 멤버만 가능)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/studies/{study_id}/schedules/past")
    public ResponseEntity<List<PastScheduleResponseDto>> getPastSchedulesForReflection(
        @PathVariable("study_id") Long studyId,
        @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(scheduleService.findPastSchedulesForReflection(studyId, user));
    }
}