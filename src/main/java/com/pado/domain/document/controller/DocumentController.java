package com.pado.domain.document.controller;

import com.pado.domain.document.dto.request.MaterialCreateRequestDto;
import com.pado.domain.document.dto.request.MaterialDeleteRequestDto;
import com.pado.domain.document.dto.response.MaterialListResponseDto;
import com.pado.domain.document.dto.response.MaterialSimpleResponseDto;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.document.Api400InvalidTitleError;
import com.pado.global.swagger.annotation.document.Api403ForbiddenMaterialOwnerOrLeaderError;
import com.pado.global.swagger.annotation.document.Api404MaterialNotFoundError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
import com.pado.global.swagger.annotation.study.Api404StudyNotFoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "07. Materials", description = "학습 자료 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    // TODO: 서비스 레이어 종속성 주입

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Api400InvalidTitleError
    @Operation(
            summary = "자료 업로드",
            description = "새로운 학습 자료를 업로드합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
            responseCode = "201", description = "학습 자료 업로드 성공"
    )
    @PostMapping("/studies/{study_id}/materials")
    public ResponseEntity<Void> uploadMaterial(
            @Parameter(description = "자료를 업로드할 스터디의 ID", required = true, example = "1")
            @PathVariable("study_id") Long studyId,
            @Valid @RequestBody MaterialCreateRequestDto request
    ) {
        // TODO: 학습 자료 업로드 기능 구현
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Api400InvalidTitleError
    @Api403ForbiddenMaterialOwnerOrLeaderError
    @Api404MaterialNotFoundError
    @Operation(
            summary = "자료 수정",
            description = "이미 업로드된 학습 자료의 정보를 수정합니다. (자료 작성자 또는 스터디 리더만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "자료 수정 성공"
    )
    @PutMapping("/materials/{material_id}")
    public ResponseEntity<Void> updateMaterial(
            @Parameter(description = "수정할 자료의 ID", required = true, example = "1")
            @PathVariable("material_id") Long materialId,
            @Valid @RequestBody MaterialCreateRequestDto request
    ) {
        // TODO: 자료 수정 로직 구현
        return ResponseEntity.ok().build();
    }

    @Api403ForbiddenMaterialOwnerOrLeaderError
    @Api404MaterialNotFoundError
    @Operation(
            summary = "자료 삭제",
            description = "특정 학습 자료들을 삭제합니다. (자료 작성자 또는 스터디 리더만 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "자료 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    name = "자료 ID 유효성 오류",
                                    value = "{\"error_code\": \"INVALID_MATERIAL_IDS\", \"field\": \"material_ids\", \"message\": \"자료 ID 목록은 최소 한 개 이상 포함되어야 합니다.\"}"
                            )))
    })
    @DeleteMapping("/materials")
    public ResponseEntity<Void> deleteMaterials(
            @Valid @RequestBody MaterialDeleteRequestDto request
    ) {
        // TODO: 자료 삭제 로직 구현
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "자료 목록 조회",
            description = "업로드된 학습 자료 목록을 조회합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(
            responseCode = "200", description = "자료 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = MaterialListResponseDto.class))
    )
    @GetMapping("/studies/{study_id}/materials")
    public ResponseEntity<MaterialListResponseDto> getMaterials(
            @Parameter(description = "조회할 스터디의 ID", required = true, example = "1")
            @PathVariable("study_id") Long studyId,
            @Parameter(description = "필터링할 카테고리 목록 (쉼표로 구분)", example = "강의,코드")
            @RequestParam(required = false) String category,
            @Parameter(description = "페이지 번호 (0부터 시작)", required = true, example = "0")
            @RequestParam int page,
            @Parameter(description = "페이지 당 사이즈 (기본값 10)")
            @RequestParam(defaultValue = "10") int size
    ) {
        // TODO: 자료 목록 조회 로직 구현
        List<MaterialSimpleResponseDto> mockMaterials = List.of(
                new MaterialSimpleResponseDto(1L, "스프링 웹 강의 자료", "강의", List.of("https://pado-storage.com/data1.pdf"), LocalDateTime.now().minusDays(5)),
                new MaterialSimpleResponseDto(2L, "JPA 핵심 코드", "코드", List.of("https://pado-storage.com/data2.zip"), LocalDateTime.now().minusDays(2))
        );
        boolean hasNext = page < 3;
        MaterialListResponseDto mockResponse = new MaterialListResponseDto(mockMaterials, page, size, hasNext);

        return ResponseEntity.ok(mockResponse);
    }
}