package com.pado.domain.study.controller;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
import com.pado.domain.study.dto.response.MyStudyResponseDto;
import com.pado.domain.study.dto.response.StudyDetailResponseDto;
import com.pado.domain.study.dto.response.StudyListResponseDto;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.service.StudyMemberService;
import com.pado.domain.study.service.StudyService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyLeaderOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. Study", description = "스터디 생성, 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/studies")
public class StudyController {

    private final StudyService studyService;
    private final StudyMemberService studyMemberService;

    @Operation(summary = "스터디 생성", description = "새로운 스터디를 생성 (스터디 이름, 한 줄 소개, 스터디 설명, 카테고리, 제한 인원, 이미지)")
    @ApiResponse(
        responseCode = "201", description = "스터디 생성 성공"
    )
    @PostMapping
    public ResponseEntity<Void> createStudy(
        @Parameter(hidden = true) @CurrentUser User user,
        @Valid @RequestBody StudyCreateRequestDto request) {
        studyService.createStudy(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @SecurityRequirements({})
    @Operation(summary = "스터디 목록 조회 및 검색",
        description = """
            검색어 및 필터링 조건을 통해 '모집 중'인 스터디 목록을 조회합니다.
            - **로그인 사용자**: 필터 적용 여부 + 사용자의 관심사/지역/최신성/검색어 등을 종합한 추천 점수 순으로 정렬됩니다.
            - **비로그인 사용자**: 최신순으로 정렬됩니다.
            """)
    @ApiResponse(responseCode = "200", description = "스터디 목록 조회 성공",
        content = @Content(schema = @Schema(implementation = StudyListResponseDto.class)))
    @Parameters({
        @Parameter(name = "keyword", description = "검색할 키워드 (제목 또는 한 줄 소개, 선택 사항)", example = "스프링"),
        @Parameter(name = "interests", description = "필터링할 카테고리(관심 분야) 목록. 여러 개 가능.", example = "프로그래밍,취업"),
        @Parameter(name = "locations", description = "필터링할 지역 목록. 여러 개 가능.", example = "서울,경기"),
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", required = true, example = "0"),
        @Parameter(name = "size", description = "페이지 당 사이즈 (기본값 10)", example = "10")
    })
    @GetMapping
    public ResponseEntity<StudyListResponseDto> findStudies(
        @Parameter(hidden = true) @CurrentUser User user,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) List<Category> interests,
        @RequestParam(required = false) List<Region> locations,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        StudyListResponseDto response = studyService.findStudies(user, keyword, interests,
            locations, page, size);
        return ResponseEntity.ok(response);
    }

    @SecurityRequirements({})
    @Api404StudyNotFoundError
    @Operation(summary = "스터디 상세 정보 조회", description = "지정된 ID를 가진 스터디의 상세 정보를 조회합니다.")
    @ApiResponse(
        responseCode = "200", description = "스터디 상세 정보 조회 성공",
        content = @Content(schema = @Schema(implementation = StudyDetailResponseDto.class))
    )
    @Parameters({
        @Parameter(name = "study_id", description = "조회할 스터디의 ID", required = true, example = "1")
    })
    @GetMapping("/{study_id}")
    public ResponseEntity<StudyDetailResponseDto> getStudyDetail(
        @PathVariable("study_id") Long studyId
    ) {
        StudyDetailResponseDto responseDto = studyService.getStudyDetail(studyId);
        return ResponseEntity.ok(responseDto);
    }

    @Api403ForbiddenStudyLeaderOnlyError
    @Api404StudyNotFoundError
    @Operation(
        summary = "스터디 정보 수정",
        description = "지정된 스터디의 정보를 수정합니다. (스터디 리더만 가능)"
    )
    @ApiResponse(
        responseCode = "200", description = "스터디 정보 수정 성공"
    )
    @Parameters({
        @Parameter(name = "study_id", description = "수정할 스터디의 ID", required = true, example = "1")
    })
    @PatchMapping("/{study_id}")
    public ResponseEntity<Void> updateStudy(
        @Parameter(hidden = true) @CurrentUser User user,
        @PathVariable("study_id") Long studyId,
        @Valid @RequestBody StudyCreateRequestDto request
    ) {
        studyService.updateStudy(user, studyId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디 자진 탈퇴", description = "스터디 멤버가 스스로 스터디를 탈퇴합니다.")
    @ApiResponse(responseCode = "204", description = "스터디 탈퇴 성공")
    @Parameters({
        @Parameter(name = "study_id", description = "탈퇴할 스터디의 ID", required = true, example = "1")
    })
    @DeleteMapping("/{study_id}/membership")
    public ResponseEntity<Void> leaveStudy(
        @Parameter(hidden = true) @CurrentUser User user,
        @PathVariable("study_id") Long studyId) {
        studyService.leaveStudy(user, studyId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "스터디 신청 취소", description = "사용자가 승인 대기 중인 스터디 신청을 스스로 취소합니다.")
    @ApiResponse(responseCode = "204", description = "신청 취소 성공")
    @Parameters({
        @Parameter(name = "study_id", description = "신청을 취소할 스터디의 ID", required = true, example = "1")
    })
    @DeleteMapping("/{study_id}/applications/me")
    public ResponseEntity<Void> cancelApplication(
        @Parameter(hidden = true) @CurrentUser User user,
        @PathVariable("study_id") Long studyId
    ) {
        studyMemberService.cancelApplication(user, studyId);
        return ResponseEntity.noContent().build();
    }
}