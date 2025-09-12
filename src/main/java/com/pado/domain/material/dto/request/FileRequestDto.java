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

        @Schema(description = "파일 키(업로드용 Presigned URL 발급 시 함께 받은 파일 키)")
        String key
) {}