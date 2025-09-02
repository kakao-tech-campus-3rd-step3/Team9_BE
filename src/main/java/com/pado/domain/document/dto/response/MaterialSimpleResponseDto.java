package com.pado.domain.document.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "학습 자료 간략 정보 DTO")
public record MaterialSimpleResponseDto(
        @Schema(description = "자료 ID", example = "1")
        Long id,

        @Schema(description = "자료 제목", example = "스프링 강의 자료")
        String title,

        @Schema(description = "자료 카테고리", example = "강의")
        String category,

        @Schema(description = "자료 URL 리스트", example = "[\"https://pado-storage.com/data1.pdf\"]")
        List<String> data_urls,

        @Schema(description = "마지막 수정일", example = "2025-09-02T13:30:00")
        LocalDateTime updated_at
) {}
