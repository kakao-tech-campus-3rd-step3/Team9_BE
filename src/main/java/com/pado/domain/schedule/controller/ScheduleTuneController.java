package com.pado.domain.schedule.controller;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneResponseDto;
import com.pado.domain.schedule.service.ScheduleTuneService;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.schedule.Api404TuningScheduleNotFoundError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    @Operation(
        summary = "조율 생성",
        description = "스터디 리더가 조율을 생성합니다(기간/가용 시간대/슬롯 간격으로 슬롯을 자동 생성하고, 멤버별 candidate_number를 부여합니다).",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            description = "조율 생성 요청",
            content = @Content(
                schema = @Schema(implementation = ScheduleTuneCreateRequestDto.class),
                examples = @ExampleObject(
                    name = "create_tune",
                    value = """
                        {
                          "title": "주간 정기 회의",
                          "content": "안건: 진행상황 점검",
                          "start_date": "2025-09-21",
                          "end_date": "2025-09-21",
                          "available_start_time": "10:00",
                          "available_end_time": "12:00"
                        }
                        """
                )
            )
        )
    )
    @ApiResponse(responseCode = "201", description = "조율 생성 성공")
    @ApiResponse(responseCode = "400", description = "유효성 오류", content = @Content(
        schema = @Schema(implementation = ErrorResponseDto.class),
        examples = @ExampleObject(
            name = "INVALID_INPUT",
            value = """
                { "code":"INVALID_INPUT","message":"end_date는 start_date 이후여야 합니다.","errors":null,"timestamp":"2025-09-19T10:00:00","path":"/api/studies/1/schedule-tunes" }
                """
        )
    ))
    @Parameters({
        @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1")
    })
    @PostMapping("/studies/{study_id}/schedule-tunes")
    public ResponseEntity<Void> createScheduleTune(
        @PathVariable("study_id") Long studyId,
        @Valid @org.springframework.web.bind.annotation.RequestBody ScheduleTuneCreateRequestDto request
    ) {
        scheduleTuneService.createScheduleTune(studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
        summary = "조율 목록",
        description = "스터디의 진행 중(PENDING)인 조율 목록을 조회합니다(완료 건은 기본적으로 제외).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
        schema = @Schema(implementation = ScheduleTuneResponseDto.class),
        examples = @ExampleObject(
            name = "list_tunes",
            value = """
                [
                  { "title":"주간 정기 회의","start":"2025-09-21T10:00:00","end":"2025-09-21T12:00:00" }
                ]
                """
        )
    ))
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
    @Operation(
        summary = "조율 상세",
        description = "조율 상세 정보를 조회합니다(슬롯별 OR 비트맵을 [binary_number] 배열로 반환하고, 참가자 이름/비트 값을 함께 제공합니다).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(
        schema = @Schema(implementation = ScheduleTuneDetailResponseDto.class),
        examples = @ExampleObject(
            name = "tune_detail",
            value = """
                {
                  "title":"주간 정기 회의",
                  "description":"안건: 진행상황 점검",
                  "candidate_dates":,[1]
                  "available_start_time":"2025-09-21T10:00:00",
                  "available_end_time":"2025-09-21T12:00:00",
                  "participants":[{"id":1,"name":"Alice","candidate_number":1}]
                }
                """
        )
    ))
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
    @Operation(
        summary = "조율 참여/갱신",
        description = "멤버가 가능한 슬롯을 제출합니다(배열 길이는 슬롯 수와 일치해야 하며, 각 값은 0/1로 해석됩니다).",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            description = "슬롯 선택(0/1 배열)",
            content = @Content(
                schema = @Schema(implementation = ScheduleTuneParticipantRequestDto.class),
                examples = @ExampleObject(
                    name = "participate",
                    value = """
                        { "candidate_dates": }[1]
                        """
                )
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "참여 반영 성공", content = @Content(
        schema = @Schema(implementation = ScheduleTuneParticipantResponseDto.class),
        examples = @ExampleObject(
            name = "participate_ok",
            value = """
                { "message":"updated" }
                """
        )
    ))
    @ApiResponse(responseCode = "400", description = "유효성 오류", content = @Content(
        schema = @Schema(implementation = ErrorResponseDto.class),
        examples = @ExampleObject(
            name = "INVALID_INPUT",
            value = """
                { "code":"INVALID_INPUT","message":"candidate_dates 길이가 슬롯 수와 다릅니다.","errors":null,"timestamp":"2025-09-19T10:00:00","path":"/api/studies/1/schedule-tunes/100" }
                """
        )
    ))
    @Parameters({
        @Parameter(name = "study_id", description = "스터디 ID", required = true, example = "1"),
        @Parameter(name = "tune_id", description = "조율 ID", required = true, example = "1234")
    })
    @PostMapping("/studies/{study_id}/schedule-tunes/{tune_id}")
    public ResponseEntity<ScheduleTuneParticipantResponseDto> participateInScheduleTune(
        @PathVariable("study_id") Long studyId,
        @PathVariable("tune_id") Long tuneId,
        @Valid @org.springframework.web.bind.annotation.RequestBody ScheduleTuneParticipantRequestDto request
    ) {
        return ResponseEntity.ok(scheduleTuneService.participate(studyId, tuneId, request));
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404TuningScheduleNotFoundError
    @Operation(
        summary = "조율 완료",
        description = "요청한 시작/종료가 생성된 슬롯 경계와 정확히 일치해야 합니다(일치 시 일정 생성 + status=COMPLETED 전이).",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @RequestBody(
            required = true,
            description = "완료 요청(슬롯 경계와 동일해야 함)",
            content = @Content(
                schema = @Schema(implementation = ScheduleCreateRequestDto.class),
                examples = @ExampleObject(
                    name = "complete",
                    value = """
                        {
                          "title":"확정 회의",
                          "content":"최종 안건",
                          "start_time":"2025-09-21T10:00:00",
                          "end_time":"2025-09-21T11:00:00"
                        }
                        """
                )
            )
        )
    )
    @ApiResponse(responseCode = "200", description = "완료 성공", content = @Content(
        schema = @Schema(implementation = ScheduleCompleteResponseDto.class),
        examples = @ExampleObject(
            name = "complete_ok",
            value = """
                { "success":true }
                """
        )
    ))
    @ApiResponse(responseCode = "400", description = "유효성 오류", content = @Content(
        schema = @Schema(implementation = ErrorResponseDto.class),
        examples = @ExampleObject(
            name = "INVALID_INPUT",
            value = """
                { "code":"INVALID_INPUT","message":"선택 시간이 생성된 슬롯과 일치하지 않습니다.","errors":null,"timestamp":"2025-09-19T10:00:00","path":"/api/schedule-tunes/100/complete" }
                """
        )
    ))
    @Parameters({
        @Parameter(name = "tune_id", description = "조율 ID", required = true, example = "1234")
    })
    @PutMapping("/schedule-tunes/{tune_id}/complete")
    public ResponseEntity<ScheduleCompleteResponseDto> completeScheduleTune(
        @PathVariable("tune_id") Long tuneId,
        @Valid @org.springframework.web.bind.annotation.RequestBody ScheduleCreateRequestDto request
    ) {
        return ResponseEntity.ok(scheduleTuneService.complete(tuneId, request));
    }
}