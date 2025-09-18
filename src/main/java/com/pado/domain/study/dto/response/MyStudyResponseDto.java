package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 스터디 목록 응답 DTO")
public record MyStudyResponseDto(
        @Schema(description = "스터디 ID", example = "1")
        Long study_id,

        @Schema(description = "스터디 제목", example = "JPA 스터디")
        String title
) {
}