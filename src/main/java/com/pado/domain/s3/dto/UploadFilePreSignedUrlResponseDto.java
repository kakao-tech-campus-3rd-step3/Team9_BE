package com.pado.domain.s3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "S3 Pre-signed URL 발급 응답 DTO")
public record UploadFilePreSignedUrlResponseDto(
        @JsonProperty("presigned_url")
        @Schema(
                name = "presigned_url",
                description = "파일을 S3에 직접 업로드할 때 사용할 임시 URL (PUT 요청)"
        )
        String presignedUrl,

        @JsonProperty("file_key")
        @Schema(
                name = "file_key",
                description = "파일을 다운로드할 때 사용될 키"
        )
        String fileKey
) {
}
