package com.pado.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "회고 목록 조회 응답 DTO")
public record ReflectionListResponseDto(
    @Schema(description = "회고 정보 리스트")
    List<?> reflections,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 당 데이터 수", example = "10")
    int size,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {

}