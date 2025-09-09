package com.pado.domain.material.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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

        @Schema(description = "주차 정보 (학습자료에만 존재)", example = "1", nullable = true)
        Integer week,

        @Schema(description = "작성자 ID", example = "1")
        Long userId,

        @Schema(description = "작성자 닉네임", example = "김민수")
        String nickname,

        @JsonProperty("data_urls")
        @Schema(name = "data_urls", description = "자료 URL 리스트", example = "[\"https://pado-storage.com/data1.pdf\"]")
        List<String> dataUrls,

        @JsonProperty("created_at")
        @Schema(name = "created_at", description = "자료 생성일", example = "2025-09-02T13:30:00")
        LocalDateTime createdAt
) {}
