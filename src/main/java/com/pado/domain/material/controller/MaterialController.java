package com.pado.domain.material.controller;

import com.pado.domain.material.dto.request.MaterialRequestDto;
import com.pado.domain.material.dto.response.MaterialDetailResponseDto;
import com.pado.domain.material.dto.response.MaterialListResponseDto;
import com.pado.domain.material.service.MaterialService;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.material.Api403ForbiddenMaterialOwnerOrLeaderError;
import com.pado.global.swagger.annotation.material.Api404MaterialNotFoundError;
import com.pado.global.swagger.annotation.study.Api403ForbiddenStudyMemberOnlyError;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "07. Materials", description = "학습 자료 관련 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Api403ForbiddenStudyMemberOnlyError
    @Api404StudyNotFoundError
    @Operation(
            summary = "자료 업로드",
            description = "S3에 파일 업로드 후, 자료 정보를 최종적으로 DB에 업로드합니다. (스터디 멤버만 가능)"
    )
    @ApiResponse(responseCode = "201", description = "학습 자료 업로드 성공")
    @Parameters({
            @Parameter(name = "study_id", description = "자료를 업로드할 스터디의 ID", required = true, example = "1")
    })
    @PostMapping("/studies/{study_id}/materials")
    public ResponseEntity<MaterialDetailResponseDto> createMaterial(
            @PathVariable("study_id") Long studyId,
            @Valid @RequestBody MaterialRequestDto request
    ) {
        MaterialDetailResponseDto response = materialService.createMaterial(studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Api403ForbiddenMaterialOwnerOrLeaderError
    @Api404MaterialNotFoundError
    @Operation(
            summary = "자료 수정",
            description = "이미 업로드된 학습 자료의 정보를 수정합니다. (자료 작성자만 가능)"
    )
    @ApiResponse(responseCode = "200", description = "자료 수정 성공")
    @Parameters({
            @Parameter(name = "material_id", description = "수정할 자료의 ID", required = true, example = "1")
    })
    @PutMapping("/materials/{material_id}")
    public ResponseEntity<MaterialDetailResponseDto> updateMaterial(
            @PathVariable("material_id") Long materialId,
            @Valid @RequestBody MaterialRequestDto request
    ) {
        MaterialDetailResponseDto response = materialService.updateMaterial(materialId, request);
        return ResponseEntity.ok(response);
    }

    @Api403ForbiddenMaterialOwnerOrLeaderError
    @Api404MaterialNotFoundError
    @Operation(
            summary = "자료 삭제",
            description = "특정 학습 자료들을 삭제합니다. (자료 작성자만 가능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "자료 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    name = "자료 ID 유효성 오류",
                                    value = """
                                        {
                                          "code": "INVALID_MATERIAL_IDS",
                                          "message": "자료 ID가 올바르지 않습니다.",
                                          "errors": [
                                            "material_ids: 자료 ID 목록은 최소 한 개 이상 포함되어야 합니다."
                                          ],
                                          "timestamp": "2025-09-07T08:15:30.123Z",
                                          "path": "/api/materials/delete"
                                        }
                                        """
                            )
                    )
            )
    })
    @Parameters({
            @Parameter(name = "material_ids", description = "삭제할 자료 ID 목록 (쉼표로 구분)", required = true, example = "1,2,3")
    })
    @DeleteMapping("/materials")
    public ResponseEntity<Void> deleteMaterials(
            @RequestParam("material_ids")
            @NotEmpty(message = "자료 ID 목록은 최소 한 개 이상 포함되어야 합니다.")
            List<Long> materialIds
    ) {
        materialService.deleteMaterial(materialIds);
        return ResponseEntity.noContent().build();
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
    @Parameters({
            @Parameter(name = "study_id", description = "조회할 스터디의 ID", required = true, example = "1"),
            @Parameter(name = "category", description = "필터링할 카테고리 목록 (쉼표로 구분). '전체'를 포함하면 모든 카테고리 조회", example = "공지,학습자료 또는 전체"),
            @Parameter(name = "week", description = "필터링할 주차 목록 (쉼표로 구분, 학습자료에만 적용)", example = "1,2,3"),
            @Parameter(name = "keyword", description = "검색 키워드 (제목, 내용에서 검색)", example = "스프링"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "페이지 당 사이즈 (기본값: 10)", example = "10"),
            @Parameter(name = "sort", description = "정렬 기준 (기본값: createdAt,desc)", example = "createdAt,desc")
    })
    @GetMapping("/studies/{study_id}/materials")
    public ResponseEntity<MaterialListResponseDto> getMaterials(
            @PathVariable("study_id") Long studyId,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) List<String> week,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        MaterialListResponseDto response = materialService.findAllMaterials(
                studyId,
                category,
                week,
                keyword,
                pageable
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "자료 상세 조회",
            description = "특정 학습 자료의 상세 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "자료 상세 조회 성공")
    @Api404MaterialNotFoundError
    @Parameters({
            @Parameter(name = "material_id", description = "조회할 자료의 ID", required = true, example = "1")
    })
    @GetMapping("/materials/{material_id}")
    public ResponseEntity<MaterialDetailResponseDto> getMaterialDetail(
            @PathVariable("material_id") Long materialId
    ) {
        MaterialDetailResponseDto response = materialService.findMaterialById(materialId);
        return ResponseEntity.ok(response);
    }
}