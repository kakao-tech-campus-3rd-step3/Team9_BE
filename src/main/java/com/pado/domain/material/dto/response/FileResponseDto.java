package com.pado.domain.material.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record FileResponseDto(
        @Schema(description = "파일 ID", example = "1")
        Long id,

        @Schema(description = "파일 이름", example = "spring_guide.pdf")
        String name,

        @Schema(description = "파일 다운로드, 삭제 시 사용되는 키")
        String key,

        @Schema(description = "파일 크기 (단위: bytes)", example = "1024")
        Long size
) {
}
