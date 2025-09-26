package com.pado.domain.s3.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "S3 Presigned URL 생성 요청 DTO")
public record UploadFilePreSignedUrlRequestDto(
        @Schema(name = "name", description = "업로드할 파일의 원본 이름", example = "my-report.pdf")
        @NotBlank
        String name
) {
}
