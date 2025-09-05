package com.pado.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 확인 응답 DTO")
public record NicknameCheckResponseDto(
        @Schema(description = "닉네임 사용 가능 여부. 사용 가능하면 true, 중복되면 false.", example = "true")
        boolean is_available
) {
}