package com.pado.domain.s3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record DownloadFilePresignedUrlResponseDto(
        @JsonProperty("presigned_url")
        @Schema(
                name = "presigned_url",
                description = "S3에서 파일을 다운로드할 때 사용할 임시 URL (GET 요청)"
        )
        String presignedUrl
) {
}
