package com.pado.domain.s3.controller;

import com.pado.global.swagger.annotation.common.NoApi409Conflict;
import com.pado.domain.s3.dto.PreSignedUrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03. Upload", description = "파일 업로드 관련 유틸리티 API")
@RestController
@RequestMapping("/api/upload")
public class S3Controller {

    // TODO: 서비스 레이어 종속성 주입

    @NoApi409Conflict
    @Operation(summary = "데이터 저장 임시 url 발급", description = "프로필 이미지 등 파일을 S3에 직접 업로드하기 위한 임시 url 주소를 받아옵니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "URL 발급 성공",
            content = @Content(schema = @Schema(implementation = PreSignedUrlResponseDto.class))))
    @PostMapping("/pre")
    public ResponseEntity<PreSignedUrlResponseDto> getPreSignedUrl() {
        /// TODO: Pre-signed URL 생성 로직 구현
        String mockUrl = "https://pado-bucket.s3.ap-northeast-2.amazonaws.com/images/profiles/sadhbf123-asjdgasi29-asjd?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIOSFODNN7EXAMPLE%2F20250902%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Date=20250902T005100Z&X-Amz-Expires=900&X-Amz-SignedHeaders=host&X-Amz-Signature=f0e8a8b8c8d8e8f8g8h8i8j8k8l8m8n8o8p8q8r8s8t8u8v8w8x8y8z8a8b8c8d8";
        return ResponseEntity.ok(new PreSignedUrlResponseDto(mockUrl));
    }
}


