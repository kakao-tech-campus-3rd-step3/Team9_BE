package com.pado.domain.material.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "학습 자료 목록 조회 응답 DTO")
public record MaterialListResponseDto(
        @Schema(description = "학습 자료 목록")
        List<MaterialSimpleResponseDto> materials,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 당 자료 수", example = "10")
        int size,

        @JsonProperty("has_next")
        @Schema(name = "has_next", description = "다음 페이지 존재 여부 (무한 스크롤)", example = "true")
        boolean hasNext
) {}
