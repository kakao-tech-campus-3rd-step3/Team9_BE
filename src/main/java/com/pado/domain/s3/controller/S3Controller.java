package com.pado.domain.s3.controller;

import com.pado.domain.s3.dto.DownloadFilePresignedUrlRequestDto;
import com.pado.domain.s3.dto.DownloadFilePresignedUrlResponseDto;
import com.pado.domain.s3.dto.UploadFilePreSignedUrlRequestDto;
import com.pado.domain.s3.dto.UploadFilePreSignedUrlResponseDto;
import com.pado.domain.s3.service.S3Service;
import com.pado.global.swagger.annotation.common.NoApi409Conflict;
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
@RequestMapping("/api")
public class S3Controller {

    private final S3Service s3Service;

    @NoApi409Conflict
    @Operation(summary = "파일 데이터 저장 임시 url 발급", description = "자료 파일을 S3에 직접 업로드하기 위한 임시 url 주소를 받아옵니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "URL 발급 성공",
            content = @Content(schema = @Schema(implementation = UploadFilePreSignedUrlResponseDto.class))))
    @PostMapping("/upload/files")
    public ResponseEntity<UploadFilePreSignedUrlResponseDto> createUploadFilePreSignedUrl(
            @Valid @RequestBody UploadFilePreSignedUrlRequestDto request
    ) {
        UploadFilePreSignedUrlResponseDto response = s3Service.createUploadPresignedUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @NoApi409Conflict
    @Operation(summary = "파일 데이터 다운로드 임시 url 발급", description = "S3에 저장된 파일들을 가져오기 위해 임시 url 주소를 받아옵니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "URL 발급 성공",
            content = @Content(schema = @Schema(implementation = DownloadFilePresignedUrlResponseDto.class))))
    @PostMapping("/download/files")
    public ResponseEntity<DownloadFilePresignedUrlResponseDto> createDownloadFilePreSignedUrl(
            @Valid @RequestBody DownloadFilePresignedUrlRequestDto request
    ) {
        DownloadFilePresignedUrlResponseDto response = s3Service.createDownloadPresignedUrl(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}


