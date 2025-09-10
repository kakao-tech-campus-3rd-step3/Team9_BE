package com.pado.global.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "S3 Pre-signed URL 발급 응답 DTO")
public record PreSignedUrlResponseDto(
        @Schema(description = "파일을 업로드할 임시 URL (이 URL로 PUT 요청)", example = "https://pado-bucket.s3.ap-northeast-2.amazonaws.com/images/profiles/sadhbf123-asjdgasi29-asjd?X-Amz-Algorithm=...")
        String preSignedUrl
) {
}
