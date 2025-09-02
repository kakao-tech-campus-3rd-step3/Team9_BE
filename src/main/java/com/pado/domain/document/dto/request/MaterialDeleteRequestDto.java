package com.pado.domain.document.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "학습 자료 삭제 요청 DTO")
public record MaterialDeleteRequestDto(
        @Schema(description = "삭제할 자료의 ID 목록", example = "[1, 2, 3]")
        @NotNull(message = "자료 ID 목록은 필수 입력 항목입니다.")
        @NotEmpty(message = "자료 ID 목록은 최소 한 개 이상 포함되어야 합니다.")
        List<Long> material_ids
) {}
