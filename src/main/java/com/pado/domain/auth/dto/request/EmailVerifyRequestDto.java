package com.pado.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증번호 확인 요청 DTO")
public record EmailVerifyRequestDto(
        @Schema(description = "인증을 진행할 이메일 주소", example = "pado@example.com")
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "사용자가 입력한 인증번호", example = "123456")
        @NotBlank(message = "인증번호는 필수 입력 항목입니다.")
        @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
        String verification_code
) {
}
