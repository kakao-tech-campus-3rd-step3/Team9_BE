package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개별 랭커 정보 DTO")
public record RankerResponseDto(
        @Schema(description = "스터디 내 순위", example = "1")
        int rank,

        @Schema(description = "퀴즈 점수", example = "1800")
        int score,

        @Schema(description = "사용자 ID", example = "101")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "김민준")
        String userName
) {

}