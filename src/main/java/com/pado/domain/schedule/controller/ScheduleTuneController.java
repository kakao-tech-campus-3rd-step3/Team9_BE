package com.pado.domain.schedule.controller;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.*;
import com.pado.global.swagger.annotation.schedule.Api400InvalidCandidateDatesError;
import com.pado.global.swagger.annotation.schedule.Api400InvalidStartTimeError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import com.pado.global.swagger.annotation.schedule.Api404TuningScheduleNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "09. Schedule Tune", description = "일정 조율 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleTuneController {

    // TODO: 서비스 레이어 종속성 주입

    @Api400InvalidStartTimeError
    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "일정 조율 요청 생성",
            description = "스터디 멤버가 새로운 일정 조율 요청을 생성합니다."
    )
    @ApiResponse(
            responseCode = "201", description = "일정 조율 요청 생성 성공"
    )
    @PostMapping("/studies/{study_id}/schedule-tunes")
    public ResponseEntity<Void> createScheduleTune(
            @Parameter(description = "조율 요청을 생성할 스터디의 ID", required = true, example = "1")
            @PathVariable("study_id") Long studyId,
            @Valid @RequestBody ScheduleCreateRequestDto request
    ) {
        // TODO: 일정 조율 요청 생성
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "조율 중인 일정 목록 조회",
            description = "스터디에 등록된 조율 중인 일정 목록을 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "일정 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = ScheduleTuneResponseDto.class))
    )
    @GetMapping("/studies/{study_id}/schedule-tunes")
    public ResponseEntity<List<ScheduleTuneResponseDto>> getScheduleTunes(
            @Parameter(description = "일정을 조회할 스터디의 ID", required = true, example = "1")
            @PathVariable("study_id") Long studyId
    ) {
        // TODO: 조율 중인 일정 목록 조회 로직 구현
        List<ScheduleTuneResponseDto> mockTunes = List.of(
                new ScheduleTuneResponseDto("3차 스터디 모임 날짜 조율", LocalDateTime.of(2025, 9, 10, 14, 0), LocalDateTime.of(2025, 9, 10, 16, 0)),
                new ScheduleTuneResponseDto("4차 스터디 모임 장소 조율", LocalDateTime.of(2025, 9, 17, 10, 0), LocalDateTime.of(2025, 9, 17, 12, 0))
        );
        return ResponseEntity.ok(mockTunes);
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(
            summary = "조율 중인 일정 세부 조회",
            description = "지정된 조율 일정의 상세 정보를 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "세부 정보 조회 성공",
            content = @Content(schema = @Schema(implementation = ScheduleTuneDetailResponseDto.class))
    )
    @GetMapping("/studies/{study_id}/schedule-tunes/{tune_id}")
    public ResponseEntity<ScheduleTuneDetailResponseDto> getScheduleTuneDetail(
            @Parameter(description = "조회할 스터디의 ID", required = true, example = "1")
            @PathVariable("study_id") Long studyId,
            @Parameter(description = "조회할 조율 일정의 ID", required = true, example = "1234")
            @PathVariable("tune_id") Long tuneId
    ) {
        // TODO: 조율 중인 일정 세부 조회 로직 구현
        ScheduleTuneDetailResponseDto mockResponse = new ScheduleTuneDetailResponseDto("3차 스터디 모임 날짜 조율", "...", List.of(3L, 5L, 2L), LocalDateTime.now(), LocalDateTime.now(),
                List.of(
                        new ScheduleTuneParticipantDto(1L, "리더유저", 3L),
                        new ScheduleTuneParticipantDto(2L, "참여자1", 5L),
                        new ScheduleTuneParticipantDto(3L, "참여자2", 18L)
                )
        );
        return ResponseEntity.ok(mockResponse);
    }

    @Api400InvalidCandidateDatesError
    @Api403ForbiddenStudyMemberOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(
            summary = "일정 조율 개별 추가",
            description = "사용자가 일정 조율에 자신의 참여 가능 시간을 제출합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "참여 정보 업데이트 성공",
            content = @Content(schema = @Schema(implementation = ScheduleTuneParticipantResponseDto.class))
    )
    @PostMapping("/studies/{study_id}/schedule-tunes/{tune_id}")
    public ResponseEntity<ScheduleTuneParticipantResponseDto> participateInScheduleTune(
            @Parameter(description = "일정 조율을 할 스터디의 ID", required = true, example = "1")
            @PathVariable("study_id") Long studyId,
            @Parameter(description = "참여할 조율 일정의 ID", required = true, example = "1234")
            @PathVariable("tune_id") Long tuneId,
            @Valid @RequestBody ScheduleTuneParticipantRequestDto request
    ) {
        // TODO: 일정 조율 개별 추가 로직 구현
        return ResponseEntity.ok(new ScheduleTuneParticipantResponseDto("일정 조율 참여 정보가 성공적으로 업데이트되었습니다."));
    }

    @Api400InvalidStartTimeError
    @Api403ForbiddenStudyLeaderOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(
            summary = "조율 완료 처리",
            description = "조율 중인 일정을 확정된 일정으로 변환합니다. (스터디 리더만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "조율 완료 성공",
            content = @Content(schema = @Schema(implementation = ScheduleCompleteResponseDto.class))
    )
    @PutMapping("/schedule-tunes/{tune_id}/complete")
    public ResponseEntity<ScheduleCompleteResponseDto> completeScheduleTune(
            @Parameter(description = "완료할 조율 일정의 ID", required = true, example = "1234")
            @PathVariable("tune_id") Long tuneId,
            @Valid @RequestBody ScheduleCreateRequestDto request
    ) {
        // TODO: 조율 완료 처리 로직 구현
        return ResponseEntity.ok(new ScheduleCompleteResponseDto(true));
    }
}
