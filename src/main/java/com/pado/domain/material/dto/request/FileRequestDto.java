package com.pado.domain.material.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "파일 등록을 위한 첨부파일 정보")
public record FileRequestDto(
        @Schema(description = "원본 파일 이름", example = "my-report.pdf")
        @NotBlank
        String name,

        @Schema(description = "S3에 저장된 최종 파일 URL", example = "https://s3...")
        @NotBlank
        String url
) {
}
