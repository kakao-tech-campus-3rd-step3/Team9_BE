package com.pado.domain.progress.controller;

import com.pado.domain.progress.dto.ProgressChapterRequestDto;
import com.pado.domain.progress.dto.ProgressRoadMapResponseDto;
import com.pado.domain.progress.dto.ProgressStatusResponseDto;
import com.pado.domain.progress.service.ProgressService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "12. Progress", description = "진척도 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/studies")
public class ProgressController {
    private final ProgressService progressService;

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "전체 로드맵 조회",
            description = "스터디의 전체 로드맵을 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "전체 로드맵 조회 성공",
            content = @Content(schema = @Schema(implementation = ProgressRoadMapResponseDto.class))
    )
    @Parameters({
            @Parameter(name = "study_id", description = "로드맵을 조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/{study_id}/chapter")
    public ResponseEntity<ProgressRoadMapResponseDto> getRoadMap(
            @PathVariable("study_id") Long studyId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(progressService.getRoadMap(studyId, user));
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "로드맵 차시 추가",
            description = "스터디 리더가 스터디 로드맵에 새로운 차시를 추가합니다. (스터디 리더만 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "로드맵 차시 추가 성공")
    })
    @Parameters({
            @Parameter(name = "study_id", description = "로드맵을 조회할 스터디의 ID", required = true, example = "1")
    })
    @PostMapping("/{study_id}/chapter")
    public ResponseEntity<Void> createChapter(
            @PathVariable("study_id") Long studyId,
            @Valid @RequestBody ProgressChapterRequestDto request,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        progressService.createChapter(studyId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "로드맵 차시 수정",
            description = "특정 차시(Chapter)의 내용을 수정합니다. (스터디 리더만 가능)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로드맵 차시 수정 성공")

    })
    @Parameters({
            @Parameter(name = "chapter_id", description = "수정할 차시 ID", required = true, example = "1")
    })
    @PatchMapping("/{chapter_id}")
    public ResponseEntity<Void> updateChapter(
            @PathVariable("chapter_id") Long chapterId,
            @Valid @RequestBody ProgressChapterRequestDto request,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        progressService.updateChapter(chapterId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "로드맵 차시 삭제",
            description = "특정 차시(Chapter)를 삭제합니다. (스터디 리더만 가능)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "차시 삭제 성공"),
    })
    @Parameters({
            @Parameter(name = "chapter_id", description = "삭제할 차시 ID", required = true, example = "1")
    })
    @DeleteMapping("/{chapter_id}")
    public ResponseEntity<Void> deleteChapter(
            @PathVariable("chapter_id") Long chapterId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        progressService.deleteChapter(chapterId, user);
        return ResponseEntity.noContent().build();
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Operation(
            summary = "로드맵 차시 완료 처리",
            description = "특정 차시(Chapter)를 완료로 표시합니다. (스터디 리더만 가능)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "차시 완료 처리 성공")
    })
    @Parameters({
            @Parameter(name = "chapter_id", description = "완료 처리할 차시 ID", required = true, example = "1")
    })
    @PostMapping("/{chapter_id}/complete")
    public ResponseEntity<Void> completeChapter(
            @PathVariable("chapter_id") Long chapterId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        progressService.completeChapter(chapterId, user);
        return ResponseEntity.noContent().build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "스터디 개인별 현황판 조회",
            description = "특정 스터디의 전체 개인 진척도를 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "현황판 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProgressStatusResponseDto.class))),
    })
    @Parameters({
            @Parameter(name = "study_id", description = "현황판을 조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/{study_id}/status")
    public ResponseEntity<ProgressStatusResponseDto> getStudyStatus(
            @PathVariable("study_id") Long studyId,
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        return ResponseEntity.ok(progressService.getStudyStatus(studyId, user));
    }
}
