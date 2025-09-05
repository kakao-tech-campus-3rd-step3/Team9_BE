package com.pado.domain.study.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스터디 생성 요청 DTO")
public record StudyCreateRequestDto(
        @Schema(description = "스터디 제목", example = "스프링 스터디")
        @NotBlank(message = "스터디 제목은 필수 입력 항목입니다.")
        String title,

        @Schema(description = "스터디 한 줄 소개", example = "스프링 기초부터 심화까지")
        @NotBlank(message = "스터디 한 줄 소개는 필수 입력 항목입니다.")
        String description,

        @Schema(description = "스터디 상세 설명", example = "스프링의 핵심 원리부터 웹 애플리케이션 개발까지 깊이 있게 다룹니다.")
        @NotBlank(message = "스터디 상세 설명은 필수 입력 항목입니다.")
        String detail_description,

        @Schema(description = "스터디 관심 분야(카테고리) 목록", example = "[\"프로그래밍\", \"취업\"]")
        @NotNull(message = "관심 분야는 필수 입력 항목입니다.")
        String[] interests,

        @Schema(description = "최대 스터디원 수", example = "10")
        @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
        int max_members,

        @Schema(description = "스터디 대표 이미지 URL", example = "https://pado-image.com/new-study-image.jpg")
        String image_url
) {}