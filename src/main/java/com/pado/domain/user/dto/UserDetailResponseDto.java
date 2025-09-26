package com.pado.domain.user.dto;

import com.pado.domain.shared.entity.Region;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "사용자 세부 정보 응답 DTO")
public record UserDetailResponseDto(
        @Schema(description = "사용자 닉네임", example = "파도")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://your-s3-bucket.s3.amazonaws.com/images/profile.jpg")
        String image_key,

        @Schema(description = "관심 분야 목록", example = "[\"프로그래밍\", \"취업\"]")
        List<String> interests,

        @Schema(description = "활동 지역", example = "서울")
        Region location
) {
}
