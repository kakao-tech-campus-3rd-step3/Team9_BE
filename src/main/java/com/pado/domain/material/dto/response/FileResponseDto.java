package com.pado.domain.material.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record FileResponseDto(
        @Schema(description = "파일 ID", example = "1")
        Long id,

        @Schema(description = "파일 이름", example = "spring_guide.pdf")
        String name,

        @JsonProperty("file_url")
        @Schema(name = "file_url", description = "파일 접근 URL", example = "https://pado-storage.com/...")
        String fileUrl
) {
}
