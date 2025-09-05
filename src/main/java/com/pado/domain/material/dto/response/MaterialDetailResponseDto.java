package com.pado.domain.material.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record MaterialDetailResponseDto (
        @Schema(description = "자료 ID", example = "1")
        Long id,

        @Schema(description = "자료 제목", example = "스프링 강의 자료")
        String title,

        @Schema(description = "자료 카테고리", example = "자료")
        String category,

        @Schema(description = "자료 전체 내용")
        String content,

        @JsonProperty("created_at")
        @Schema(name = "created_at", description = "생성일", example = "2025-09-02T13:30:00")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        @Schema(name = "updated_at", description = "마지막 수정일", example = "2025-09-03T10:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "첨부 파일 목록")
        List<FileResponseDto> files
){}
