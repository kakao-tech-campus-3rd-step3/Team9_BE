package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "나의 랭킹 및 점수 응답 DTO")
public record MyRankResponseDto(
        @Schema(description = "나의 랭킹", example = "3")
        int my_rank,

        @Schema(description = "나의 점수", example = "1500")
        int my_score
) {

}