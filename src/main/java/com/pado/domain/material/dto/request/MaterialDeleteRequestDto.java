package com.pado.domain.material.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "학습 자료 삭제 요청 DTO")
public record MaterialDeleteRequestDto(
        @JsonProperty("material_ids")
        @Schema(name = "material_ids",description = "삭제할 자료의 ID 목록", example = "[1, 2, 3]")
        @NotNull(message = "자료 ID 목록은 필수 입력 항목입니다.")
        @NotEmpty(message = "자료 ID 목록은 최소 한 개 이상 포함되어야 합니다.")
        List<Long> materialIds
) {}

// delete는 body 대신 파라미터 사용을 권장함
// 스프링의 자동 변환 덕분에, 개발자는 이 메소드 안에서
// ids를 처음부터 List<Long>이었던 것처럼 바로 사용할 수 있습니다.
//@DeleteMapping("/api/materials")
//public ResponseEntity<Void> deleteMaterials(@RequestParam List<Long> ids) {
//        // ids 변수는 [1L, 2L, 3L] 형태의 List 객체입니다.
//        materialService.deleteMaterials(ids);
//        return ResponseEntity.noContent().build();
//}