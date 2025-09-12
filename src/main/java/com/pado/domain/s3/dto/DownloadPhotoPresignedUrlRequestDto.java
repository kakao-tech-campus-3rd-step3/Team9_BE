package com.pado.domain.s3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DownloadPhotoPresignedUrlRequestDto(

        @Schema(name = "file_key", description = "다운로드하기 위해 필요한 파일 키")
        @NotBlank
        @JsonProperty("file_key")
        String fileKey
) {
}
