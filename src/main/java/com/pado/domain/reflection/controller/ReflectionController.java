package com.pado.domain.reflection.controller;

import com.pado.domain.reflection.dto.*;
import com.pado.domain.reflection.service.ReflectionService;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.domain.user.entity.User;
import com.pado.global.swagger.annotation.reflection.Api403ForbiddenReflectionOwnerError;
import com.pado.global.swagger.annotation.reflection.Api404ReflectionNotFoundError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "13. Reflection", description = "스터디 회고 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReflectionController {

    private final ReflectionService reflectionService;

    @Operation(summary = "회고 작성", description = "특정 스터디에 대한 회고를 작성합니다. (스터디 멤버만 가능)")
    @ApiResponse(responseCode = "200", description = "회고 작성 성공")
    @Api404StudyNotFoundError
    @Api403ForbiddenStudyMemberOnlyError
    @PostMapping("/studies/{study_id}/reflections")
    public ResponseEntity<ReflectionResponseDto> createReflection(
        @Parameter(description = "회고를 작성할 스터디 ID") @PathVariable("study_id") Long studyId,
        @Parameter(hidden = true) @CurrentUser User user,
        @Valid @RequestBody ReflectionCreateRequestDto request
    ) {
        return ResponseEntity.ok(reflectionService.createReflection(studyId, user, request));
    }

    @Operation(summary = "스터디 전체 회고 조회", description = "특정 스터디에 작성된 모든 회고를 조회합니다. (스터디 멤버만 가능)")
    @ApiResponse(responseCode = "200", description = "회고 목록 조회 성공")
    @Api404StudyNotFoundError
    @Api403ForbiddenStudyMemberOnlyError
    @GetMapping("/studies/{study_id}/reflections")
    public ResponseEntity<List<ReflectionResponseDto>> getReflections(
        @Parameter(description = "회고를 조회할 스터디 ID") @PathVariable("study_id") Long studyId,
        @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(reflectionService.getReflections(studyId, user));
    }

    @Operation(summary = "회고 상세 조회", description = "특정 회고의 상세 내용을 조회합니다. (스터디 멤버만 가능)")
    @ApiResponse(responseCode = "200", description = "회고 상세 조회 성공")
    @Api404ReflectionNotFoundError
    @Api403ForbiddenStudyMemberOnlyError
    @GetMapping("/reflections/{reflection_id}")
    public ResponseEntity<ReflectionResponseDto> getReflection(
        @Parameter(description = "조회할 회고 ID") @PathVariable("reflection_id") Long reflectionId,
        @Parameter(hidden = true) @CurrentUser User user) {
        return ResponseEntity.ok(reflectionService.getReflection(reflectionId, user));
    }

    @Operation(summary = "회고 수정", description = "자신이 작성한 회고를 수정합니다. (작성자 본인만 가능)")
    @ApiResponse(responseCode = "200", description = "회고 수정 성공")
    @Api404ReflectionNotFoundError
    @Api403ForbiddenReflectionOwnerError
    @PatchMapping("/reflections/{reflection_id}")
    public ResponseEntity<ReflectionResponseDto> updateReflection(
        @Parameter(description = "수정할 회고 ID") @PathVariable("reflection_id") Long reflectionId,
        @Parameter(hidden = true) @CurrentUser User user,
        @Valid @RequestBody ReflectionCreateRequestDto request
    ) {
        return ResponseEntity.ok(reflectionService.updateReflection(reflectionId, user, request));
    }

    @Operation(summary = "회고 삭제", description = "자신이 작성한 회고를 삭제합니다. (작성자 본인만 가능)")
    @ApiResponse(responseCode = "204", description = "회고 삭제 성공")
    @Api404ReflectionNotFoundError
    @Api403ForbiddenReflectionOwnerError
    @DeleteMapping("/reflections/{reflection_id}")
    public ResponseEntity<Void> deleteReflection(
        @Parameter(description = "삭제할 회고 ID") @PathVariable("reflection_id") Long reflectionId,
        @Parameter(hidden = true) @CurrentUser User user) {
        reflectionService.deleteReflection(reflectionId, user);
        return ResponseEntity.noContent().build();
    }
}