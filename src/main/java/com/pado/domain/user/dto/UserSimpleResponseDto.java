package com.pado.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 주요 정보 응답 DTO")
public record UserSimpleResponseDto(
        @Schema(description = "사용자 닉네임", example = "파도")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://your-s3-bucket.s3.amazonaws.com/images/profile.jpg")
        String image_url
) {
}
