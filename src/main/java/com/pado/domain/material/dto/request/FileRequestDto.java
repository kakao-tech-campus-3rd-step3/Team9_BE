package com.pado.domain.material.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "파일 등록을 위한 첨부파일 정보")
public record FileRequestDto(
        @Schema(
                description = "파일 ID (기존 파일 유지 시 사용, 새 파일은 null)",
                example = "1",
                nullable = true
        )
        Long id,

        @Schema(description = "원본 파일 이름", example = "my-report.pdf")
        @NotBlank(message = "파일 이름은 필수입니다.")
        String name,

        @Schema(description = "S3에 저장된 최종 파일 URL", example = "https://s3...")
        @NotBlank(message = "파일 URL은 필수입니다.")
        String url
) {}