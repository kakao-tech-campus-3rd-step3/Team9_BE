package com.pado.domain.schedule.controller;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneResponseDto;
import com.pado.domain.schedule.service.ScheduleTuneService;
import com.pado.global.swagger.annotation.schedule.Api404TuningScheduleNotFoundError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "09. Schedule Tune", description = "스터디 일정 조율 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleTuneController {

    private final ScheduleTuneService scheduleTuneService;

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(summary = "조율 생성", description = "스터디 리더가 조율을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "조율 생성 성공")
    @Parameters({
        @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1")
    })
    @PostMapping("/studies/{study_id}/schedule-tunes")
    public ResponseEntity<Void> createScheduleTune(
        @PathVariable("study_id") Long studyId,
        @Valid @RequestBody ScheduleTuneCreateRequestDto request
    ) {
        scheduleTuneService.createScheduleTune(studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(summary = "조율 목록", description = "스터디의 진행 중(PENDING) 조율 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ScheduleTuneResponseDto.class)))
    @Parameters({
        @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1")
    })
    @GetMapping("/studies/{study_id}/schedule-tunes")
    public ResponseEntity<List<ScheduleTuneResponseDto>> getScheduleTunes(
        @PathVariable("study_id") Long studyId
    ) {
        return ResponseEntity.ok(scheduleTuneService.findAllScheduleTunes(studyId));
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(summary = "조율 상세", description = "조율 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
        content = @Content(schema = @Schema(implementation = ScheduleTuneDetailResponseDto.class)))
    @Parameters({
        @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1"),
        @Parameter(name = "tune_id", description = "조율 ID", required = true, example = "1234")
    })
    @GetMapping("/studies/{study_id}/schedule-tunes/{tune_id}")
    public ResponseEntity<ScheduleTuneDetailResponseDto> getScheduleTuneDetail(
        @PathVariable("study_id") Long studyId,
        @PathVariable("tune_id") Long tuneId
    ) {
        return ResponseEntity.ok(scheduleTuneService.findScheduleTuneDetail(studyId, tuneId));
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(summary = "조율 참여", description = "멤버가 가능 슬롯을 제출합니다. 다음 단계에서 구현됩니다.")
    @ApiResponse(responseCode = "200", description = "참여 성공",
        content = @Content(schema = @Schema(implementation = ScheduleTuneParticipantResponseDto.class)))
    @Parameters({
        @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1"),
        @Parameter(name = "tune_id", description = "조율 ID", required = true, example = "1234")
    })
    @PostMapping("/studies/{study_id}/schedule-tunes/{tune_id}")
    public ResponseEntity<ScheduleTuneParticipantResponseDto> participateInScheduleTune(
        @PathVariable("study_id") Long studyId,
        @PathVariable("tune_id") Long tuneId,
        @Valid @RequestBody ScheduleTuneParticipantRequestDto request
    ) {
        return ResponseEntity.ok(scheduleTuneService.participate(studyId, tuneId, request));
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(summary = "조율 완료", description = "최다 참여 슬롯으로 확정 일정을 생성합니다. 다음 단계에서 구현됩니다.")
    @ApiResponse(responseCode = "200", description = "완료 성공",
        content = @Content(schema = @Schema(implementation = ScheduleCompleteResponseDto.class)))
    @Parameters({
        @Parameter(name = "tune_id", description = "조율 ID", required = true, example = "1234")
    })
    @PutMapping("/schedule-tunes/{tune_id}/complete")
    public ResponseEntity<ScheduleCompleteResponseDto> completeScheduleTune(
        @PathVariable("tune_id") Long tuneId,
        @Valid @RequestBody ScheduleCreateRequestDto request
    ) {
        return ResponseEntity.ok(scheduleTuneService.complete(tuneId, request));
    }
}
