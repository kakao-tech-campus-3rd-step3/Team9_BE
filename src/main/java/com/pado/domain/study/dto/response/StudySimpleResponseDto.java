package com.pado.domain.study.dto.response;

import com.pado.domain.study.entity.Study;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 간략 정보 DTO")
public record StudySimpleResponseDto(
        @Schema(description = "스터디 ID", example = "1")
        Long id,

        @Schema(description = "스터디 대표 이미지 URL", example = "https://pado-image.com/1")
        String image_url,

        @Schema(description = "스터디 제목", example = "스프링 스터디")
        String title,

        @Schema(description = "스터디 한 줄 소개", example = "스프링 기초부터 심화까지")
        String description
) {
        public static StudySimpleResponseDto from(Study study) {
                return new StudySimpleResponseDto(
                        study.getId(),
                        study.getImageUrl(),
                        study.getTitle(),
                        study.getDescription()
                );
        }
}