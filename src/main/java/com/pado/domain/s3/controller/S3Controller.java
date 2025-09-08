package com.pado.domain.s3.controller;

import com.pado.domain.s3.dto.PreSignedUrlRequestDto;
import com.pado.domain.s3.dto.PreSignedUrlResponseDto;
import com.pado.domain.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03. Upload", description = "파일 업로드 관련 유틸리티 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/upload")
public class S3Controller {

    // TODO: 서비스 레이어 종속성 주입
    private final S3Service s3Service;

    @Operation(summary = "데이터 저장 임시 url 발급", description = "프로필 이미지 등 파일을 S3에 직접 업로드하기 위한 임시 url 주소를 받아옵니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "URL 발급 성공",
            content = @Content(schema = @Schema(implementation = PreSignedUrlResponseDto.class))))
    @PostMapping("/pre")
    public ResponseEntity<PreSignedUrlResponseDto> createPreSignedUrl(
            @Valid @RequestBody PreSignedUrlRequestDto request
    ) {
        // TODO: Pre-signed URL 생성 로직 구현
        PreSignedUrlResponseDto response = s3Service.createPresignedUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}


