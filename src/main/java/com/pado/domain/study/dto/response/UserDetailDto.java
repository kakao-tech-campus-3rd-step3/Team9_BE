package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "사용자 세부 정보 DTO")
public record UserDetailDto(
        @Schema(description = "사용자 프로필 이미지 URL", example = "https://pado-image.com/user/1")
        String image_url,

        @Schema(description = "성별", example = "Men", allowableValues = {"Men", "Women"})
        String gender,

        @Schema(description = "관심 분야", example = "[\"프로그래밍\", \"취업\"]")
        List<String> interests,

        @Schema(description = "지역", example = "서울")
        String location
) {}
