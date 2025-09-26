package com.pado.domain.s3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UploadPhotoPresignedUrlRequestDto(

        @Schema(name = "content_type", description = "업로드하기 위해 필요한 이미지 형식", example = "image/png")
        @JsonProperty("content_type")
        @NotBlank(message = "콘텐츠 타입은 비어 있을 수 없습니다.")
        @Pattern(regexp = "^image/(jpeg|png|gif|jpg)$", message = "지원하지 않는 이미지 형식입니다.")
        String contentType
) {
}
