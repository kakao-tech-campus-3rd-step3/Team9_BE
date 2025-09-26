package com.pado.domain.auth.dto.request;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.user.entity.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "회원가입 요청 DTO")
public record SignUpRequestDto(

        @Schema(description = "사용자 이메일", example = "pado@example.com")
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        String password,

        @Schema(description = "프로필 이미지 URL (없는 경우 빈 문자열)", example = "https://your-s3-bucket.s3.amazonaws.com/images/profile.jpg")
        @NotNull(message = "이미지 URL은 null일 수 없습니다.")
        String image_key,

        @Schema(description = "닉네임", example = "파도")
        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "성별", example = "MALE")
        @NotNull(message = "성별은 필수 입력 항목입니다.")
        Gender gender,

        @Schema(description = "관심 분야 목록", example = "[\"어학\", \"취업\"]")
        @NotNull(message = "관심 분야는 null일 수 없습니다.")
        @Size(min = 1, message = "관심 분야를 하나 이상 선택해주세요.")
        List<Category> interests,

        @Schema(description = "활동 지역", example = "서울")
        @NotNull(message = "활동 지역은 필수 입력 항목입니다.")
        Region region
) {

}
