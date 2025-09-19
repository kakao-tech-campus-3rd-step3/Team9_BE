package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 전체 랭킹 조회 응답 DTO")
public record TotalRankingResponseDto(
        @Schema(description = "점수 순으로 정렬된 랭커 목록")
        List<RankerResponseDto> ranking
) {

}