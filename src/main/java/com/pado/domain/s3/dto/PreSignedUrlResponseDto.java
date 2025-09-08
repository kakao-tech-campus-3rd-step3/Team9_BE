package com.pado.domain.s3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "S3 Pre-signed URL 발급 응답 DTO")
public record PreSignedUrlResponseDto(
        @JsonProperty("presigned_url")
        @Schema(
                name = "presigned_url",
                description = "파일을 S3에 직접 업로드할 때 사용할 임시 URL (PUT 요청)",
                example = "https://pado-storage.s3.ap-northeast-2.amazonaws.com/materials/random-uuid-file.png?X-Amz-Algorithm=..."
        )
        String presignedUrl,

        @JsonProperty("file_url")
        @Schema(
                name = "file_url",
                description = "업로드 완료 후 DB에 저장될 최종 파일 접근 URL",
                example = "https://pado-storage.s3.ap-northeast-2.amazonaws.com/materials/random-uuid-file.png"
        )
        String fileUrl
) {
}
