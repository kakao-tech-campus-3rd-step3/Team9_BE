package com.pado.domain.user.dto;

import com.pado.domain.study.entity.StudyMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 스터디 정보 응답 DTO")
public record UserStudyResponseDto (
        @Schema(description = "사용자 닉네임", example = "파도")
        String nickname,

        @Schema(description = "프로필 이미지 URL", example = "https://your-s3-bucket.s3.amazonaws.com/images/profile.jpg")
        String image_url,

        @Schema(description = "스터디 제목", example = "파도 스터디")
        String title,

        @Schema(description = "사용자 역할", example = "LEADER")
        StudyMemberRole role
){}
