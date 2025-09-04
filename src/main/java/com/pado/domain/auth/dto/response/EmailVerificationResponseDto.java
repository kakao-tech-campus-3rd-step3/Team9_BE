package com.pado.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이메일 인증 관련 응답 DTO")
public record EmailVerificationResponseDto(
        @Schema(description = "요청 성공 여부", example = "true")
        boolean success,

        @Schema(description = "응답 메시지", example = "인증번호가 성공적으로 전송되었습니다.")
        String message
) {
}
