package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 상세 정보 조회 응답 DTO")
public record StudyDetailResponseDto(
        @Schema(description = "스터디 대표 이미지 URL", example = "https://pado-image.com/1")
        String image_url,

        @Schema(description = "스터디 제목", example = "스프링 스터디")
        String title,

        @Schema(description = "스터디 한 줄 소개", example = "스프링 기초부터 심화까지")
        String description,

        @Schema(description = "스터디 상세 설명", example = "스프링의 핵심 원리부터 웹 애플리케이션 개발까지 깊이 있게 다룹니다.")
        String detail_description,

        @Schema(description = "스터디 관심 분야(카테고리) 목록", example = "[\"프로그래밍\", \"취업\"]")
        String[] interests,

        @Schema(description = "현재 스터디원 수", example = "5")
        int current_members,

        @Schema(description = "최대 스터디원 수", example = "10")
        int max_members
) {}
