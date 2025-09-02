package com.pado.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증번호 전송 요청 DTO")
public record EmailSendRequestDto(
        @Schema(description = "인증번호를 받을 이메일 주소", example = "pado@example.com")
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email
) {
}
